package com.rookedsysc.point.infrastructure.`in`.dto

import com.rookedsysc.point.application.dto.PointReserveCommand

data class PointReserveRequest(
    val requestId: String,
    val userId: Long,
    val amount: Long
) {
    fun toCommand(): PointReserveCommand {
        return PointReserveCommand(
            requestId = requestId,
            userId = userId,
            amount = amount
        )
    }
}
