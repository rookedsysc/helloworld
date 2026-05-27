package com.rookedsysc.order.infrastructure.kafka

import com.rookedsysc.common.kafka.KafkaConsumerGroups
import com.rookedsysc.common.kafka.KafkaTopics
import com.rookedsysc.common.kafka.dto.PointUsedEvent
import com.rookedsysc.order.application.OrderService
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.stereotype.Component
import org.springframework.transaction.support.TransactionTemplate

@Component
class PointUsedConsumer(
    private val orderService: OrderService,
    private val transactionTemplate: TransactionTemplate,
) {
    @KafkaListener(
        topics = [KafkaTopics.POINT_USED],
        groupId = KafkaConsumerGroups.POINT_USED_CONSUMER,
        properties = [
            "spring.json.value.default.type=com.rookedsysc.common.kafka.dto.PointUsedEvent",
        ],
    )
    fun handle(event: PointUsedEvent) {
        transactionTemplate.execute {
            orderService.complete(event.orderId)
        }
    }
}
