package com.rookedsysc.order.application.dto

import com.rookedsysc.order.entity.OrderStatus

data class OrderProcessingResult(
    val orderId: Long,
    val status: OrderStatus,
)
