package com.rookedsysc.order.infrastructure.out.point

import com.rookedsysc.order.infrastructure.out.point.dto.PointReserveApiRequest
import com.rookedsysc.order.infrastructure.out.point.dto.PointReserveCancelRequest
import com.rookedsysc.order.infrastructure.out.point.dto.PointReserveConfirmRequest
import org.springframework.web.client.RestClient

class PointApiClient(
    private val restClient: RestClient
) {
    fun reservePoint(request: PointReserveApiRequest) {
        restClient.post()
            .uri("/points/reservations")
            .body(request)
            .retrieve()
            .toBodilessEntity()
    }

    fun reserveConfirm(request: PointReserveConfirmRequest) {
        restClient.post()
            .uri("/points/reservations/confirm")
            .body(request)
            .retrieve()
            .toBodilessEntity()
    }

    fun reserveCancel(request: PointReserveCancelRequest) {
        restClient.post()
            .uri("/points/reservations/cancel")
            .body(request)
            .retrieve()
            .toBodilessEntity()
    }
}
