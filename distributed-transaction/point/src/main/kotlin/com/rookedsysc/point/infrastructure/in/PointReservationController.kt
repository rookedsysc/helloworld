package com.rookedsysc.point.infrastructure.`in`

import com.rookedsysc.point.application.PointReservationService
import com.rookedsysc.point.infrastructure.`in`.dto.PointReserveCancelRequest
import com.rookedsysc.point.infrastructure.`in`.dto.PointReserveConfirmRequest
import com.rookedsysc.point.infrastructure.`in`.dto.PointReserveRequest
import com.rookedsysc.point.infrastructure.`in`.dto.PointReserveResponse
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@Tag(name = "포인트 예약")
@RestController
@RequestMapping("/points/reservations")
class PointReservationController(
    private val pointReservationService: PointReservationService,
) {
    @Operation(summary = "포인트 예약 생성")
    @PostMapping
    fun reserve(@RequestBody request: PointReserveRequest): PointReserveResponse {
        val command = request.toCommand()
        val reservedAmount = pointReservationService.tryReserve(command).reservedAmount
        return PointReserveResponse(reservedAmount = reservedAmount)
    }

    @Operation(summary = "포인트 예약 확정")
    @PostMapping("/confirm")
    fun confirm(
        @RequestBody request: PointReserveConfirmRequest
    ) {
        val command = request.toCommand()
        pointReservationService.confirmReserve(command)
    }

    @Operation(summary = "포인트 예약 취소")
    @PostMapping("/cancel")
    fun cancel(@RequestBody request: PointReserveCancelRequest) {
        return pointReservationService.cancelReserve(command = request.toCommand())
    }

}
