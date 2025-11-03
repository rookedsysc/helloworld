package com.rookedsysc.point.infrastructure.`in`.dto

import com.rookedsysc.point.application.dto.PointListResult

data class PointListResponse(
    val id: Long,
    val userId: Long,
    val amount: Long,
    val availableAmount: Long
) {
    companion object {
        fun from(result: PointListResult): PointListResponse {
            return PointListResponse(
                id = result.id,
                userId = result.userId,
                amount = result.amount,
                availableAmount = result.availableAmount
            )
        }
    }
}
