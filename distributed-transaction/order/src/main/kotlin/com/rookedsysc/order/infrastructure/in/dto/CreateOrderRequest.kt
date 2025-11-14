package com.rookedsysc.order.infrastructure.`in`.dto

import com.rookedsysc.order.application.dto.CreateOrderCommand

data class CreateOrderRequest(
    val userId: Long,
    val items: List<OrderItem>
) {
    fun toCommand(): CreateOrderCommand {
        return CreateOrderCommand(
            userId = userId,
            items = items.map {
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
