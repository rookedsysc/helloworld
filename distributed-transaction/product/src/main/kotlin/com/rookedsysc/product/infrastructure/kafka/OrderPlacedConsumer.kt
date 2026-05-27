package com.rookedsysc.product.infrastructure.kafka

import com.rookedsysc.common.kafka.KafkaConsumerGroups
import com.rookedsysc.common.kafka.KafkaTopics
import com.rookedsysc.product.application.BunchProductBuyService
import com.rookedsysc.product.application.dto.BunchProductBuyCommand
import com.rookedsysc.product.application.dto.BunchProductCancelCommand
import com.rookedsysc.product.application.event.QuantityDecreasedApplicationEvent
import com.rookedsysc.product.application.event.QuantityDecreasedFailApplicationEvent
import com.rookedsysc.product.infrastructure.kafka.dto.OrderPlacedEvent
import org.springframework.context.ApplicationEventPublisher
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.stereotype.Component
import org.springframework.transaction.support.TransactionTemplate

@Component
class OrderPlacedConsumer(
    private val productService: BunchProductBuyService,
    private val eventPublisher: ApplicationEventPublisher,
    private val transactionTemplate: TransactionTemplate,
) {
    @KafkaListener(
        topics = [KafkaTopics.ORDER_PLACED],
        groupId = KafkaConsumerGroups.ORDER_PLACED_CONSUMER,
        properties = [
            "spring.json.value.default.type=com.rookedsysc.product.infrastructure.kafka.dto.OrderPlacedEvent",
        ],
    )
    fun handle(event: OrderPlacedEvent) {
        val requestId = event.orderId.toString()

        try {
            transactionTemplate.execute {
                val result = productService.buy(
                    BunchProductBuyCommand(
                        requestId = requestId,
                        productInfos = event.productInfos.map { item ->
                            BunchProductBuyCommand.ProductInfo(
                                productId = item.productId,
                                quantity = item.quantity,
                            )
                        },
                    )
                )

                eventPublisher.publishEvent(
                    QuantityDecreasedApplicationEvent(
                        orderId = event.orderId,
                        userId = event.userId,
                        totalPrice = result.totalPrice,
                    )
                )
            }
        } catch (e: Exception) {
            transactionTemplate.execute {
                productService.cancel(
                    BunchProductCancelCommand(requestId = requestId)
                )

                eventPublisher.publishEvent(
                    QuantityDecreasedFailApplicationEvent(orderId = event.orderId)
                )
            }
        }
    }
}
