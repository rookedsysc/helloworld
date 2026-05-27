package com.rookedsysc.point.infrastructure.kafka

import com.rookedsysc.common.kafka.KafkaPartitionKeys
import com.rookedsysc.common.kafka.KafkaTopics
import com.rookedsysc.common.kafka.dto.PointUseFailEvent
import com.rookedsysc.point.application.event.PointUseFailApplicationEvent
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.stereotype.Component
import org.springframework.transaction.event.TransactionPhase
import org.springframework.transaction.event.TransactionalEventListener

@Component
class PointUseFailKafkaEventListener(
    private val kafkaTemplate: KafkaTemplate<String, PointUseFailEvent>,
) {
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    fun handle(event: PointUseFailApplicationEvent) {
        kafkaTemplate.send(
            KafkaTopics.POINT_USE_FAIL,
            KafkaPartitionKeys.orderId(event.orderId),
            PointUseFailEvent(orderId = event.orderId)
        )
    }
}
