package com.rookedsysc.monolithic.order

class CreateOrderCommand(
    val orderItems: List<OrderItem>,
) {
    class OrderItem(
        val productId: Long,
        val quantity: Long
    )
}
