package com.rookedsysc.order.application.dto

data class OrderDto(
    val userId: Long,
    val orderItems: List<OrderItem>
) {

    data class OrderItem(
        val productId: Long,
        val quantity: Long
    )
}
