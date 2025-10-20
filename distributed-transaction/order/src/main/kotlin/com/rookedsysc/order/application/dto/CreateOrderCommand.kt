package com.rookedsysc.order.application.dto

class CreateOrderCommand(
    val userId: Long,
    val orderItems: List<OrderItem>,
) {
    class OrderItem(
        val productId: Long,
        val quantity: Long
    )
}
