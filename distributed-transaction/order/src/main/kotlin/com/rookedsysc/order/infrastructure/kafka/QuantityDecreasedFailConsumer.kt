package com.rookedsysc.order.infrastructure.kafka

import com.rookedsysc.common.kafka.KafkaConsumerGroups
import com.rookedsysc.common.kafka.KafkaTopics
import com.rookedsysc.common.kafka.dto.QuantityDecreasedFailEvent
import com.rookedsysc.order.application.OrderService
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.stereotype.Component

@Component
class QuantityDecreasedFailConsumer(
    private val orderService: OrderService,
) {
    @KafkaListener(
        topics = [KafkaTopics.QUANTITY_DECREASED_FAIL],
        groupId = KafkaConsumerGroups.QUANTITY_DECREASED_FAIL_CONSUMER,
        properties = [
            "spring.json.value.default.type=com.rookedsysc.common.kafka.dto.QuantityDecreasedFailEvent",
        ],
    )
    fun handle(event: QuantityDecreasedFailEvent) {
        orderService.fail(event.orderId)
    }
}
