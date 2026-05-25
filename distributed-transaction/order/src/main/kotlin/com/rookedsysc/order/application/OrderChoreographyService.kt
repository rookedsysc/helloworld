package com.rookedsysc.order.application

import com.rookedsysc.order.application.dto.PlaceOrderCommand
import com.rookedsysc.order.entity.Order
import com.rookedsysc.order.entity.OrderItem
import com.rookedsysc.order.infrastructure.kafka.OrderPlacedProducer
import com.rookedsysc.order.infrastructure.kafka.dto.OrderPlacedEvent
import com.rookedsysc.order.infrastructure.out.OrderItemRepository
import com.rookedsysc.order.infrastructure.out.OrderRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.transaction.support.TransactionSynchronization
import org.springframework.transaction.support.TransactionSynchronizationManager

@Service
class OrderChoreographyService(
    private val orderRepository: OrderRepository,
    private val orderItemRepository: OrderItemRepository,
    private val orderPlacedProducer: OrderPlacedProducer,
) {

    @Transactional
    fun placeOrder(command: PlaceOrderCommand) {
        val order: Order = orderRepository.findById(command.orderId).orElseThrow {
            RuntimeException("Order not found")
        }
        val orderItems: List<OrderItem> = orderItemRepository.findAllByOrderId(order.id)
        val productInfos: List<OrderPlacedEvent.ProductInfo> = orderItems.map { orderItem ->
            OrderPlacedEvent.ProductInfo(
                productId = orderItem.productId,
                quantity = orderItem.quantity,
            )
        }

        order.request()
        orderRepository.save(order)

        TransactionSynchronizationManager.registerSynchronization(
            object : TransactionSynchronization {
                override fun afterCommit() {
                    orderPlacedProducer.send(
                        OrderPlacedEvent(
                            orderId = command.orderId,
                            userId = order.userId,
                            productInfos = productInfos,
                        )
                    )
                }
            }
        )
    }
}
