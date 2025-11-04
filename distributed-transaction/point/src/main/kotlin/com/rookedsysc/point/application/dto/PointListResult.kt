package com.rookedsysc.point.application.dto

import com.rookedsysc.point.domain.Point


data class PointListResult(
    val id: Long,
    val userId: Long,
    val amount: Long,
) {
    companion object {
        fun from(point: Point): PointListResult {
            return PointListResult(
                id = point.id,
                userId = point.userId,
                amount = point.amount,
            )
        }
    }
}
