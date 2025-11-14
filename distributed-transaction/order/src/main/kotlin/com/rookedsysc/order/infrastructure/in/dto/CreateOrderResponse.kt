package com.rookedsysc.order.infrastructure.`in`.dto

import com.rookedsysc.order.application.dto.CreateOrderResult

data class CreateOrderResponse(val orderId: Long) {
    companion object {
        fun of(result: CreateOrderResult): CreateOrderResponse {
            return CreateOrderResponse(
                orderId = result.orderId,
            )
        }
    }
}
