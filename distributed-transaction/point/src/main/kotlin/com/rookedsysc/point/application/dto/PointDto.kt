package com.rookedsysc.point.application.dto

/**
 * 포인트 충전 요청 DTO
 */
data class ChargePointRequest(
    val userId: Long,
    val amount: Long
)

/**
 * 포인트 응답 DTO
 */
data class PointResponse(
    val id: Long,
    val userId: Long,
    val amount: Long
)
