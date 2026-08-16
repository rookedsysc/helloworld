package com.rookedsysc.order.infrastructure.out.point.dto

data class PointUseApiRequest(
    val requestId: String,
    val userId: Long,
    val amount: Long
)
