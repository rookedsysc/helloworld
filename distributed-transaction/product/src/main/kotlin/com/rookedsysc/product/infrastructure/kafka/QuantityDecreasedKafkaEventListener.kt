package com.rookedsysc.product.infrastructure.kafka

import com.rookedsysc.common.kafka.KafkaPartitionKeys
import com.rookedsysc.common.kafka.KafkaTopics
import com.rookedsysc.product.application.event.QuantityDecreasedApplicationEvent
import com.rookedsysc.product.infrastructure.kafka.dto.QuantityDecreasedEvent
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.stereotype.Component
import org.springframework.transaction.event.TransactionPhase
import org.springframework.transaction.event.TransactionalEventListener

@Component
class QuantityDecreasedKafkaEventListener(
    private val kafkaTemplate: KafkaTemplate<String, QuantityDecreasedEvent>,
) {
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    fun handle(event: QuantityDecreasedApplicationEvent) {
        kafkaTemplate.send(
            KafkaTopics.QUANTITY_DECREASED,
            KafkaPartitionKeys.orderId(event.orderId),
            QuantityDecreasedEvent(
                orderId = event.orderId,
                userId = event.userId,
                totalPrice = event.totalPrice,
            )
        )
    }
}
