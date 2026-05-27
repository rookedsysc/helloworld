package com.rookedsysc.order.application

import com.rookedsysc.order.application.dto.PlaceOrderCommand
import com.rookedsysc.order.application.event.OrderPlacedApplicationEvent
import com.rookedsysc.order.entity.Order
import com.rookedsysc.order.entity.OrderItem
import com.rookedsysc.order.infrastructure.out.OrderItemRepository
import com.rookedsysc.order.infrastructure.out.OrderRepository
import org.springframework.context.ApplicationEventPublisher
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class OrderChoreographyService(
    private val orderRepository: OrderRepository,
    private val orderItemRepository: OrderItemRepository,
    private val eventPublisher: ApplicationEventPublisher,
) {

    @Transactional
    fun placeOrder(command: PlaceOrderCommand) {
        val order: Order = orderRepository.findById(command.orderId).orElseThrow {
            RuntimeException("Order not found")
        }
        val orderItems: List<OrderItem> = orderItemRepository.findAllByOrderId(order.id)
        val productInfos: List<OrderPlacedApplicationEvent.ProductInfo> = orderItems.map { orderItem ->
            OrderPlacedApplicationEvent.ProductInfo(
                productId = orderItem.productId,
                quantity = orderItem.quantity,
            )
        }

        order.request()
        orderRepository.save(order)

        eventPublisher.publishEvent(
            OrderPlacedApplicationEvent(
                orderId = command.orderId,
                userId = order.userId,
                productInfos = productInfos,
            )
        )
    }
}
