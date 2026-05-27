package com.rookedsysc.product.application.event

data class QuantityDecreasedApplicationEvent(
    val orderId: Long,
    val userId: Long,
    val totalPrice: Long,
)
