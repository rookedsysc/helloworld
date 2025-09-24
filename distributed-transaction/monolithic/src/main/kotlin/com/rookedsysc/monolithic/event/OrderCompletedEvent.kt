package com.rookedsysc.monolithic.event

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonProperty

data class OrderCompletedEvent @JsonCreator constructor(
    @JsonProperty("orderId") val orderId: Long,
    @JsonProperty("userId") val userId: Long,
    @JsonProperty("totalPrice") val totalPrice: Long
)