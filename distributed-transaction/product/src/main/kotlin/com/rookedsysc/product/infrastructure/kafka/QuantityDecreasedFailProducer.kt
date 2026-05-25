package com.rookedsysc.product.infrastructure.kafka

import com.rookedsysc.common.kafka.KafkaPartitionKeys
import com.rookedsysc.common.kafka.KafkaTopics
import com.rookedsysc.common.kafka.dto.QuantityDecreasedFailEvent
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.stereotype.Component

@Component
class QuantityDecreasedFailProducer(
    private val kafkaTemplate: KafkaTemplate<String, QuantityDecreasedFailEvent>,
) {
    fun send(event: QuantityDecreasedFailEvent) {
        kafkaTemplate.send(
            KafkaTopics.QUANTITY_DECREASED_FAIL,
            KafkaPartitionKeys.orderId(event.orderId),
            event,
        )
    }
}
