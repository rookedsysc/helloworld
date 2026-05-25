package com.rookedsysc.product.infrastructure.kafka

import com.rookedsysc.common.kafka.KafkaConsumerGroups
import com.rookedsysc.common.kafka.KafkaTopics
import com.rookedsysc.common.kafka.dto.PointUseFailEvent
import com.rookedsysc.common.kafka.dto.QuantityDecreasedFailEvent
import com.rookedsysc.product.application.BunchProductBuyService
import com.rookedsysc.product.application.dto.BunchProductCancelCommand
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.stereotype.Component

@Component
class PointUseFailConsumer(
    private val productService: BunchProductBuyService,
    private val quantityDecreasedFailProducer: QuantityDecreasedFailProducer,
) {
    @KafkaListener(
        topics = [KafkaTopics.POINT_USE_FAIL],
        groupId = KafkaConsumerGroups.POINT_USE_FAIL_CONSUMER,
        properties = [
            "spring.json.value.default.type=com.rookedsysc.common.kafka.dto.PointUseFailEvent",
        ],
    )
    fun handle(event: PointUseFailEvent) {
        val requestId = event.orderId.toString()

        productService.cancel(BunchProductCancelCommand(requestId = requestId))

        quantityDecreasedFailProducer.send(
            QuantityDecreasedFailEvent(orderId = event.orderId)
        )
    }
}
