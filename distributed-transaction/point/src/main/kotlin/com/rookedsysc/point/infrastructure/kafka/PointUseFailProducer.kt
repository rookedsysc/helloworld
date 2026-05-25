package com.rookedsysc.point.infrastructure.kafka

import com.rookedsysc.common.kafka.KafkaPartitionKeys
import com.rookedsysc.common.kafka.KafkaTopics
import com.rookedsysc.common.kafka.dto.PointUseFailEvent
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.stereotype.Component

@Component
class PointUseFailProducer(
    private val kafkaTemplate: KafkaTemplate<String, PointUseFailEvent>,
) {
    fun send(event: PointUseFailEvent) {
        kafkaTemplate.send(
            KafkaTopics.POINT_USE_FAIL,
            KafkaPartitionKeys.orderId(event.orderId),
            event,
        )
    }
}
