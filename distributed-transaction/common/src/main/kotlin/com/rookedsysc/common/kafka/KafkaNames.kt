package com.rookedsysc.common.kafka

object KafkaTopics {
    const val ORDER_PLACED = "order-placed"
}

object KafkaPartitionKeys {
    fun orderId(orderId: Long): String = orderId.toString()
}
