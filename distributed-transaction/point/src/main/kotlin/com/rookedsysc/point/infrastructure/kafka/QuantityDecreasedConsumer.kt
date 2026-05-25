package com.rookedsysc.point.infrastructure.kafka

import com.rookedsysc.common.kafka.KafkaConsumerGroups
import com.rookedsysc.common.kafka.KafkaTopics
import com.rookedsysc.common.kafka.dto.PointUsedEvent
import com.rookedsysc.point.application.PointUseService
import com.rookedsysc.point.application.dto.PointUseCancelCommand
import com.rookedsysc.point.application.dto.PointUseCommand
import com.rookedsysc.point.infrastructure.kafka.dto.PointUseFailEvent
import com.rookedsysc.point.infrastructure.kafka.dto.QuantityDecreasedEvent
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.stereotype.Component

@Component
class QuantityDecreasedConsumer(
    private val pointUseService: PointUseService,
    private val pointUsedProducer: PointUsedProducer,
    private val pointUseFailProducer: PointUseFailProducer,
) {
    @KafkaListener(
        topics = [KafkaTopics.QUANTITY_DECREASED],
        groupId = KafkaConsumerGroups.QUANTITY_DECREASED_CONSUMER,
        properties = [
            "spring.json.value.default.type=com.rookedsysc.point.infrastructure.kafka.dto.QuantityDecreasedEvent",
        ],
    )
    fun handle(event: QuantityDecreasedEvent) {
        val requestId = event.orderId.toString()

        try {
            pointUseService.use(
                PointUseCommand(
                    requestId = requestId,
                    userId = 1L,
                    amount = event.totalPrice,
                )
            )

            pointUsedProducer.send(PointUsedEvent(orderId = event.orderId))
        } catch (e: Exception) {
            pointUseService.cancel(PointUseCancelCommand(requestId = requestId))

            pointUseFailProducer.send(PointUseFailEvent(orderId = event.orderId))
        }
    }
}
