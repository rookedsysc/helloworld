package com.rookedsysc.point.infrastructure.kafka

import com.rookedsysc.common.kafka.KafkaPartitionKeys
import com.rookedsysc.common.kafka.KafkaTopics
import com.rookedsysc.common.kafka.dto.PointUsedEvent
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.stereotype.Component

@Component
class PointUsedProducer(
    private val kafkaTemplate: KafkaTemplate<String, PointUsedEvent>,
) {
    fun send(event: PointUsedEvent) {
        kafkaTemplate.send(
            KafkaTopics.POINT_USED,
            KafkaPartitionKeys.orderId(event.orderId),
            event,
        )
    }
}
