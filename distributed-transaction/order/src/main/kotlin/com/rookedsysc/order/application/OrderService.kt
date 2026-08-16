package com.rookedsysc.order.application

import com.rookedsysc.order.application.dto.CreateOrderCommand
import com.rookedsysc.order.application.dto.CreateOrderResult
import com.rookedsysc.order.application.dto.OrderDto
import com.rookedsysc.order.entity.Order
import com.rookedsysc.order.entity.OrderItem
import com.rookedsysc.order.infrastructure.out.OrderItemRepository
import com.rookedsysc.order.infrastructure.out.OrderRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class OrderService(
    private val orderRepository: OrderRepository,
    private val orderItemRepository: OrderItemRepository,
) {

    @Transactional
    fun createOrder(command: CreateOrderCommand): CreateOrderResult {
        val order: Order = orderRepository.save(
            Order(
                userId = command.userId
            )
        )

        val orderItems: List<OrderItem> = command.items.map { item ->
            OrderItem(orderId = order.id, productId = item.productId, quantity = item.quantity)
        }

        orderItemRepository.saveAll(orderItems)

        return CreateOrderResult(order.id)
    }

    fun getOrder(orderId: Long): OrderDto {
        val order: Order = orderRepository.findById(orderId).orElseThrow {
            RuntimeException("Order not found")
        }
        val orderItems: List<OrderItem> = orderItemRepository.findAllByOrderId(orderId)

        return OrderDto(
            userId = order.userId, orderItems = orderItems.map { item ->
                OrderDto.OrderItem(
                    productId = item.productId, quantity = item.quantity
                )
            }
        )
    }


    @Transactional
    fun request(orderId: Long) {
        val order = orderRepository.findById(orderId).orElseThrow()

        order.request()
        orderRepository.save(order)
    }

    @Transactional
    fun complete(orderId: Long) {
        val order = orderRepository.findById(orderId).orElseThrow()

        order.complete()
        orderRepository.save(order)
    }

    @Transactional
    fun fail(orderId: Long) {
        val order = orderRepository.findById(orderId).orElseThrow()

        order.fail()
        orderRepository.save(order)
    }
}
