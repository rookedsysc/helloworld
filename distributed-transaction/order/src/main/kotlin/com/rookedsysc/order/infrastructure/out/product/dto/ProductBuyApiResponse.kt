package com.rookedsysc.order.infrastructure.out.product.dto

/**
 * Product 서비스의 상품 예약 API 응답 DTO
 *
 * Single Responsibility: Product API 응답 데이터 전송에만 집중
 */
data class ProductBuyApiResponse(
    val totalPrice: Long
)
