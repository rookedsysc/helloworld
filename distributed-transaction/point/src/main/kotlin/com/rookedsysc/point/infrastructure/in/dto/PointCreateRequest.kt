package com.rookedsysc.point.infrastructure.`in`.dto

import com.rookedsysc.point.application.dto.PointCreateCommand

data class PointCreateRequest(
    val userId: Long,
    val amount: Long
) {
    fun toCommand() =
        PointCreateCommand(
            userId = this.userId,
            amount = this.amount
        )
}
