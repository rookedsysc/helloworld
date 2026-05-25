package com.rookedsysc.order.infrastructure.kafka

import com.rookedsysc.common.kafka.KafkaConsumerGroups
import com.rookedsysc.common.kafka.KafkaTopics
import com.rookedsysc.common.kafka.dto.PointUsedEvent
import com.rookedsysc.order.application.OrderService
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.stereotype.Component

@Component
class PointUsedConsumer(
    private val orderService: OrderService,
) {
    @KafkaListener(
        topics = [KafkaTopics.POINT_USED],
        groupId = KafkaConsumerGroups.POINT_USED_CONSUMER,
        properties = [
            "spring.json.value.default.type=com.rookedsysc.common.kafka.dto.PointUsedEvent",
        ],
    )
    fun handle(event: PointUsedEvent) {
        orderService.complete(event.orderId)
    }
}
