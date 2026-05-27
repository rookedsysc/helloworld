package com.rookedsysc.order.infrastructure.`in`.dto

import com.rookedsysc.order.application.dto.OrderProcessingResult
import com.rookedsysc.order.entity.OrderStatus

data class OrderProcessingResultResponse(
    val orderId: Long,
    val status: OrderStatus,
) {
    companion object {
        fun of(result: OrderProcessingResult): OrderProcessingResultResponse {
            return OrderProcessingResultResponse(
                orderId = result.orderId,
                status = result.status,
            )
        }
    }
}
