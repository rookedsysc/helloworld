package com.rookedsysc.point.infrastructure.kafka

import com.rookedsysc.common.kafka.KafkaConsumerGroups
import com.rookedsysc.common.kafka.KafkaTopics
import com.rookedsysc.point.application.PointUseService
import com.rookedsysc.point.application.dto.PointUseCancelCommand
import com.rookedsysc.point.application.dto.PointUseCommand
import com.rookedsysc.point.application.event.PointUseFailApplicationEvent
import com.rookedsysc.point.application.event.PointUsedApplicationEvent
import com.rookedsysc.point.infrastructure.kafka.dto.QuantityDecreasedEvent
import org.springframework.context.ApplicationEventPublisher
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.stereotype.Component
import org.springframework.transaction.support.TransactionTemplate

@Component
class QuantityDecreasedConsumer(
    private val pointUseService: PointUseService,
    private val eventPublisher: ApplicationEventPublisher,
    private val transactionTemplate: TransactionTemplate,
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
            transactionTemplate.execute {
                pointUseService.use(
                    PointUseCommand(
                        requestId = requestId,
                        userId = event.userId,
                        amount = event.totalPrice,
                    )
                )

                eventPublisher.publishEvent(PointUsedApplicationEvent(orderId = event.orderId))
            }
        } catch (e: Exception) {
            transactionTemplate.execute {
                pointUseService.cancel(PointUseCancelCommand(requestId = requestId))

                eventPublisher.publishEvent(PointUseFailApplicationEvent(orderId = event.orderId))
            }
        }
    }
}
