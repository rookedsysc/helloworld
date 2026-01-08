package com.rookedsysc.order.infrastructure.out.product.dto

/**
 * Product 서비스의 상품 예약 확정 API 요청 DTO
 *
 * Single Responsibility: Product API 요청 데이터 전송에만 집중
 */
data class ProductReserveConfirmRequest(
    val requestId: String
)
