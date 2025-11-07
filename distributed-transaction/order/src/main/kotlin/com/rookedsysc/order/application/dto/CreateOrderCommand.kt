package com.rookedsysc.order.application.dto

data class CreateOrderCommand(
    val userId: Long,
    val items: List<OrderItem>
) {
    data class OrderItem(
        val productId: Long,
        val quantity: Long
    )
}

