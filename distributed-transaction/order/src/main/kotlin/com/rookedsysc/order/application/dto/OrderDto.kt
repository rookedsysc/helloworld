package com.rookedsysc.order.application.dto

data class OrderDto(
    val userId: Long,
    val orderItems: List<OrderItemDto>
) {
    data class OrderItemDto(
        val productId: Long,
        val quantity: Long
    )
}
