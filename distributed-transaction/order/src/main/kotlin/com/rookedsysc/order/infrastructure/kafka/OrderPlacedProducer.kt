package com.rookedsysc.order.infrastructure.kafka

import com.rookedsysc.common.kafka.KafkaPartitionKeys
import com.rookedsysc.common.kafka.KafkaTopics
import com.rookedsysc.order.infrastructure.kafka.dto.OrderPlacedEvent
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.stereotype.Component

@Component
class OrderPlacedProducer(
    private val kafkaTemplate: KafkaTemplate<String, OrderPlacedEvent>,
) {
    fun send(event: OrderPlacedEvent) {
        kafkaTemplate.send(
            KafkaTopics.ORDER_PLACED,
            KafkaPartitionKeys.orderId(event.orderId),
            event,
        )
    }
}
