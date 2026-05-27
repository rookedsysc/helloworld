package com.rookedsysc.product.infrastructure.kafka.dto

data class QuantityDecreasedEvent(
    val orderId: Long,
    val userId: Long,
    val totalPrice: Long,
)
