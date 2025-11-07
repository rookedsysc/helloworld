package com.rookedsysc.order.infrastructure.out.point

import com.rookedsysc.order.infrastructure.out.point.dto.PointUseApiRequest
import com.rookedsysc.order.infrastructure.out.point.dto.PointUseCancelRequest
import org.springframework.retry.annotation.Backoff
import org.springframework.retry.annotation.Retryable
import org.springframework.web.client.HttpClientErrorException
import org.springframework.web.client.RestClient

class PointApiClient(
    private val restClient: RestClient
) {
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
    fun use(request: PointUseApiRequest) {
        restClient.post()
            .uri("/points/use")
            .body(request)
            .retrieve()
            .toBodilessEntity()
    }

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
    fun cancel(request: PointUseCancelRequest) {
        restClient.post()
            .uri("/points/cancel")
            .body(request)
            .retrieve()
            .toBodilessEntity()
    }
}
