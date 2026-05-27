package com.rookedsysc.order.infrastructure.kafka

import com.rookedsysc.common.kafka.KafkaPartitionKeys
import com.rookedsysc.common.kafka.KafkaTopics
import com.rookedsysc.order.application.event.OrderPlacedApplicationEvent
import com.rookedsysc.order.infrastructure.kafka.dto.OrderPlacedEvent
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.stereotype.Component
import org.springframework.transaction.event.TransactionPhase
import org.springframework.transaction.event.TransactionalEventListener

@Component
class OrderPlacedKafkaEventListener(
    private val kafkaTemplate: KafkaTemplate<String, OrderPlacedEvent>,
) {
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    fun handle(event: OrderPlacedApplicationEvent) {
        kafkaTemplate.send(
            KafkaTopics.ORDER_PLACED,
            KafkaPartitionKeys.orderId(event.orderId),
            OrderPlacedEvent(
                orderId = event.orderId,
                userId = event.userId,
                productInfos = event.productInfos.map { productInfo ->
                    OrderPlacedEvent.ProductInfo(
                        productId = productInfo.productId,
                        quantity = productInfo.quantity,
                    )
                },
            )
        )
    }
}
