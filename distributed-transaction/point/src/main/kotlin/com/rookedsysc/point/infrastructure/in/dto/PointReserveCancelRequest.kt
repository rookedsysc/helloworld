package com.rookedsysc.point.infrastructure.`in`.dto

import com.rookedsysc.point.application.dto.PointReserveCancelCommand

data class PointReserveCancelRequest(
    val requestId: String
) {
    fun toCommand(): PointReserveCancelCommand {
        return PointReserveCancelCommand(
            requestId = requestId
        )
    }
}
