package com.rookedsysc.product.infrastructure.kafka

import com.rookedsysc.common.kafka.KafkaConsumerGroups
import com.rookedsysc.common.kafka.KafkaTopics
import com.rookedsysc.common.kafka.dto.PointUseFailEvent
import com.rookedsysc.common.kafka.dto.QuantityDecreasedFailEvent
import com.rookedsysc.product.application.BunchProductBuyService
import com.rookedsysc.product.application.dto.BunchProductCancelCommand
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.stereotype.Component
import org.springframework.transaction.support.TransactionSynchronization
import org.springframework.transaction.support.TransactionSynchronizationManager
import org.springframework.transaction.support.TransactionTemplate

@Component
class PointUseFailConsumer(
    private val productService: BunchProductBuyService,
    private val quantityDecreasedFailProducer: QuantityDecreasedFailProducer,
    private val transactionTemplate: TransactionTemplate,
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

        transactionTemplate.execute {
            productService.cancel(BunchProductCancelCommand(requestId = requestId))

            TransactionSynchronizationManager.registerSynchronization(
                object : TransactionSynchronization {
                    override fun afterCommit() {
                        quantityDecreasedFailProducer.send(
                            QuantityDecreasedFailEvent(orderId = event.orderId)
                        )
                    }
                }
            )
        }
    }
}
