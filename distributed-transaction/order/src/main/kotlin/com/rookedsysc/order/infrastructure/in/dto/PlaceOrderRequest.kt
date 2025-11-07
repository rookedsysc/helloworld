package com.rookedsysc.order.infrastructure.`in`.dto

import com.rookedsysc.order.application.dto.PlaceOrderCommand

data class PlaceOrderRequest(
    val orderId: Long
) {
    fun toCommand() : PlaceOrderCommand {
        return PlaceOrderCommand(
            orderId = orderId
        )
    }
}
