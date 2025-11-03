package com.rookedsysc.order.infrastructure.out.product.dto

/**
 * Product 서비스의 상품 예약 API 요청 DTO
 *
 * Single Responsibility: Product API 요청 데이터 전송에만 집중
 */
data class ProductReserveApiRequest(
    val requestId: String,
    val items: List<ReserveItem>
) {
    data class ReserveItem(
        val productId: Long,
        val reserveQuantity: Long
    )
}
