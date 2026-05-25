package com.rookedsysc.product.infrastructure.kafka

import com.rookedsysc.common.kafka.KafkaPartitionKeys
import com.rookedsysc.common.kafka.KafkaTopics
import com.rookedsysc.product.infrastructure.kafka.dto.QuantityDecreasedEvent
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.stereotype.Component

@Component
class QuantityDecreasedProducer(
    private val kafkaTemplate: KafkaTemplate<String, QuantityDecreasedEvent>,
) {
    fun send(event: QuantityDecreasedEvent) {
        kafkaTemplate.send(
            KafkaTopics.QUANTITY_DECREASED,
            KafkaPartitionKeys.orderId(event.orderId),
            event,
        )
    }
}
