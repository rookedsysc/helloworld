package com.rookedsysc.order.infrastructure.out.product

import com.rookedsysc.order.infrastructure.out.product.dto.ProductReserveApiRequest
import com.rookedsysc.order.infrastructure.out.product.dto.ProductReserveApiResponse
import com.rookedsysc.order.infrastructure.out.product.dto.ProductReserveCancelRequest
import com.rookedsysc.order.infrastructure.out.product.dto.ProductReserveConfirmRequest
import org.springframework.retry.annotation.Backoff
import org.springframework.retry.annotation.Retryable
import org.springframework.stereotype.Component
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
    open fun reserveProduct(request: ProductReserveApiRequest): ProductReserveApiResponse {
        return restClient.post()
            .uri("/products/reservations")
            .body(request)
            .retrieve()
            .body(ProductReserveApiResponse::class.java)
            ?: throw IllegalStateException("Failed to reserve product: response body is null")
    }

    /**
     * 상품 예약을 확정합니다.
     *
     * @param request 상품 예약 확정 요청 정보
     */
    open fun reserveConfirm(request: ProductReserveConfirmRequest) {
        restClient.post()
            .uri("/products/reservations/confirm")
            .body(request)
            .retrieve()
            .toBodilessEntity()
    }

    /**
     * 상품 예약을 취소합니다.
     *
     * @param request 상품 예약 취소 요청 정보
     */
    open fun reserveCancel(request: ProductReserveCancelRequest) {
        restClient.post()
            .uri("/products/reservations/cancel")
            .body(request)
            .retrieve()
            .toBodilessEntity()
    }
}
