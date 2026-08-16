package com.rookedsysc.point.application.dto

data class PointReserveCommand(
    val requestId: String,
    val userId: Long,
    val amount: Long
)
