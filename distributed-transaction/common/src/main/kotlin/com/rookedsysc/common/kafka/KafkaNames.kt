package com.rookedsysc.common.kafka

object KafkaTopics {
    const val ORDER_PLACED = "order-placed"
    const val QUANTITY_DECREASED = "quantity-decreased"
    const val QUANTITY_DECREASED_FAIL = "quantity-decreased-fail"
    const val POINT_USED = "point-used"
    const val POINT_USE_FAIL = "point-use-fail"
}

object KafkaConsumerGroups {
    const val ORDER_PLACED_CONSUMER = "order-placed-consumer"
    const val QUANTITY_DECREASED_CONSUMER = "quantity-decreased-consumer"
    const val QUANTITY_DECREASED_FAIL_CONSUMER = "quantity-decreased-fail-consumer"
    const val POINT_USED_CONSUMER = "point-used-consumer"
}

object KafkaPartitionKeys {
    fun orderId(orderId: Long): String = orderId.toString()
}
