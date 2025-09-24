package com.rookedsysc.monolithic.order

class CreateOrderRequest(
    val orderItems: List<OrderItem>
) {
    fun toCommand(): CreateOrderCommand{
        return CreateOrderCommand(
            orderItems = orderItems.map {
                CreateOrderCommand.OrderItem(
                    productId = it.productId,
                    quantity = it.quantity
                )
            }
        )
    }

    class OrderItem(
        val productId: Long,
        val quantity: Long
    )
}
