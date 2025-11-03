package com.rookedsysc.order.infrastructure.`in`.dto

import com.rookedsysc.order.application.dto.CreateOrderCommand

data class CreateOrderRequest(
    val userId: Long,
    val orderItems: List<OrderItem>
) {
    fun toCommand(): CreateOrderCommand {
        return CreateOrderCommand(
            userId = userId,
            orderItems = orderItems.map {
                CreateOrderCommand.OrderItem(
                    productId = it.productId,
                    quantity = it.quantity
                )
            }
        )
    }

    data class OrderItem(
        val productId: Long,
        val quantity: Long
    )
}
