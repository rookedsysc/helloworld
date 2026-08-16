package com.rookedsysc.order.application

import com.rookedsysc.order.application.dto.CreateOrderCommand
import com.rookedsysc.order.application.dto.CreateOrderResult
import com.rookedsysc.order.application.dto.OrderDto
import com.rookedsysc.order.domain.Order
import com.rookedsysc.order.domain.OrderItem
import com.rookedsysc.order.infrastructure.out.OrderItemRepository
import com.rookedsysc.order.infrastructure.out.OrderRepository
import jakarta.transaction.Transactional
import org.springframework.stereotype.Service

@Service
class OrderService(
    private val orderRepository: OrderRepository,
    private val orderItemRepository: OrderItemRepository
) {

    fun getOrder(orderId: Long): OrderDto {
        // order 존재여부 확인
        val order: Order = orderRepository.findById(orderId).orElseThrow {
            RuntimeException("Order Id $orderId 를 찾지 못했습니다.")
        }

        val orderItems: List<OrderItem> = orderItemRepository.findAllByOrderId(orderId)
        return OrderDto(
            userId = order.userId,
            orderItems = orderItems.map {
                OrderDto.OrderItemDto(
                    productId = it.productId,
                    quantity = it.quantity
                )
            }
        )
    }

    @Transactional
    fun createOrder(command: CreateOrderCommand): CreateOrderResult {
        val order: Order = orderRepository.save(Order(userId = command.userId))
        val orderItems: List<OrderItem> = command.orderItems.map {
            OrderItem(
                orderId = order.id,
                productId = it.productId,
                quantity = it.quantity
            )
        }
        orderItemRepository.saveAll(orderItems)
        return CreateOrderResult(order.id)
    }

    @Transactional
    fun reserve(orderId: Long) {
        val order: Order = orderRepository.findById(orderId).orElseThrow {
            RuntimeException("Order Id $orderId 를 찾지 못했습니다.")
        }
        order.reserve()
        orderRepository.save(order)
    }

    @Transactional
    fun confirm(orderId: Long) {
        val order: Order = orderRepository.findById(orderId).orElseThrow {
            RuntimeException("Order Id $orderId 를 찾지 못했습니다.")
        }
        order.confirm()
        orderRepository.save(order)
    }

    @Transactional
    fun cancel(orderId: Long) {
        val order: Order = orderRepository.findById(orderId).orElseThrow {
            RuntimeException("Order Id $orderId 를 찾지 못했습니다.")
        }

        order.cancel()
        orderRepository.save(order)
    }

    @Transactional
    fun pending(orderId: Long) {
        val order: Order = orderRepository.findById(orderId).orElseThrow {
            RuntimeException("Order Id $orderId 를 찾지 못했습니다.")
        }

        order.pending()
        orderRepository.save(order)
    }
}
