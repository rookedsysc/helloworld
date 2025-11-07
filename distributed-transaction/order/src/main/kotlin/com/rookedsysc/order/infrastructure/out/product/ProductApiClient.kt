package com.rookedsysc.order.infrastructure.out.product

import com.rookedsysc.order.infrastructure.out.product.dto.ProductBuyApiRequest
import com.rookedsysc.order.infrastructure.out.product.dto.ProductBuyApiResponse
import com.rookedsysc.order.infrastructure.out.product.dto.ProductBuyCancelRequest
import com.rookedsysc.order.infrastructure.out.product.dto.ProductBuyCancelResponse
import org.springframework.retry.annotation.Backoff
import org.springframework.retry.annotation.Retryable
import org.springframework.web.client.HttpClientErrorException
import org.springframework.web.client.RestClient

/**
 * Product 서비스 API 클라이언트
 *
 * SOLID Principles Applied:
 * - Single Responsibility: Product 서비스와의 HTTP 통신만 담당
 * - Dependency Inversion: RestClient 추상화에 의존 (구체적인 구현체가 아닌)
 * - Open/Closed: 새로운 API 메서드 추가 시 기존 코드 수정 없이 확장 가능
 *
 * @property restClient Product 서비스와 통신하기 위한 RestClient
 */
open class ProductApiClient(
    private val restClient: RestClient
) {
    /**
     * 상품 예약을 요청합니다.
     *
     * @param request 상품 예약 요청 정보
     * @return 총 상품 가격 정보
     */
    @Retryable(
        retryFor = [Exception::class],
        // client error인 경우 재시도 하지 않음
        noRetryFor = [HttpClientErrorException::class],
        maxAttempts = 3,
        backoff = Backoff(
            delay = 1000L,
            multiplier = 2.0,
            random = true // 1.0 < jitter <= 2.0
        )
    )
    open fun buy(request: ProductBuyApiRequest): ProductBuyApiResponse {
        return restClient.post()
            .uri("/products/buy")
            .body(request)
            .retrieve()
            .body(ProductBuyApiResponse::class.java)
            ?: throw IllegalStateException("Failed to reserve product: response body is null")
    }

    /**
     * 상품 예약을 취소합니다.
     *
     * @param request 상품 예약 취소 요청 정보
     */
    @Retryable(
        retryFor = [Exception::class],
        noRetryFor = [HttpClientErrorException::class],
        maxAttempts = 3,
        backoff = Backoff(
            delay = 1000L,
            multiplier = 2.0,
            random = true // 1.0 < jitter <= 2.0
        )
    )
    open fun cancel(request: ProductBuyCancelRequest): ProductBuyCancelResponse {
        return restClient.post()
            .uri("/products/cancel")
            .body(request)
            .retrieve()
            .body(ProductBuyCancelResponse::class.java)
            ?: throw IllegalStateException("Failed to cancel product reservation: response body is null")
    }
}
