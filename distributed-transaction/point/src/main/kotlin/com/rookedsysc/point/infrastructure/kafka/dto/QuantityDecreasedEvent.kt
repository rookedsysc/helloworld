package com.rookedsysc.point.infrastructure.kafka.dto

data class QuantityDecreasedEvent(
    val orderId: Long,
    val totalPrice: Long,
)
