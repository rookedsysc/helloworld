package com.rookedsysc.product.infrastructure.kafka

import com.rookedsysc.common.kafka.KafkaPartitionKeys
import com.rookedsysc.common.kafka.KafkaTopics
import com.rookedsysc.common.kafka.dto.QuantityDecreasedFailEvent
import com.rookedsysc.product.application.event.QuantityDecreasedFailApplicationEvent
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.stereotype.Component
import org.springframework.transaction.event.TransactionPhase
import org.springframework.transaction.event.TransactionalEventListener

@Component
class QuantityDecreasedFailKafkaEventListener(
    private val kafkaTemplate: KafkaTemplate<String, QuantityDecreasedFailEvent>,
) {
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    fun handle(event: QuantityDecreasedFailApplicationEvent) {
        kafkaTemplate.send(
            KafkaTopics.QUANTITY_DECREASED_FAIL,
            KafkaPartitionKeys.orderId(event.orderId),
            QuantityDecreasedFailEvent(orderId = event.orderId)
        )
    }
}
