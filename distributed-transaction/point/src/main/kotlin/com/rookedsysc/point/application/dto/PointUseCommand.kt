package com.rookedsysc.point.application.dto

data class PointUseCommand(
    val requestId: String,
    val userId: Long,
    val amount: Long
)
