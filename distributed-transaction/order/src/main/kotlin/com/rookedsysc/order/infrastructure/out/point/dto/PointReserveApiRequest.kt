package com.rookedsysc.order.infrastructure.out.point.dto

data class PointReserveApiRequest(
    val requestId: String,
    val userId: Long,
    val reserveAmount: Long
)
