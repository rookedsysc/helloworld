package com.rookedsysc.point.infrastructure.`in`.dto

import com.rookedsysc.point.application.dto.PointUseCommand

data class PointUseRequest(
    val requestId: String, val userId: Long, val amount: Long
) {
    fun toCommand(): PointUseCommand {
        return PointUseCommand(
            requestId = requestId, userId = userId, amount = amount
        )
    }
}
