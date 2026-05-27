package com.rookedsysc.point.infrastructure.kafka

import com.rookedsysc.common.kafka.KafkaPartitionKeys
import com.rookedsysc.common.kafka.KafkaTopics
import com.rookedsysc.common.kafka.dto.PointUsedEvent
import com.rookedsysc.point.application.event.PointUsedApplicationEvent
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.stereotype.Component
import org.springframework.transaction.event.TransactionPhase
import org.springframework.transaction.event.TransactionalEventListener

@Component
class PointUsedKafkaEventListener(
    private val kafkaTemplate: KafkaTemplate<String, PointUsedEvent>,
) {
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    fun handle(event: PointUsedApplicationEvent) {
        kafkaTemplate.send(
            KafkaTopics.POINT_USED,
            KafkaPartitionKeys.orderId(event.orderId),
            PointUsedEvent(orderId = event.orderId)
        )
    }
}
