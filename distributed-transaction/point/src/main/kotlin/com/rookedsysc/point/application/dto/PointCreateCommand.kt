package com.rookedsysc.point.application.dto

data class PointCreateCommand(
    val userId: Long,
    val amount: Long
)
