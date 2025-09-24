package com.rookedsysc.monolithic.order

import com.fasterxml.jackson.databind.ObjectMapper
import com.rookedsysc.monolithic.point.PointService
import com.rookedsysc.monolithic.product.ProductService
import jakarta.transaction.Transactional
import org.springframework.stereotype.Service

@Service
@Transactional
class OrderService(
    private val orderRepository: OrderRepository,
    private val orderItemRepository: OrderItemRepository,
    private val productService: ProductService,
    private val pointService: PointService,
    private val objectMapper: ObjectMapper
) {

    fun createOrder(createOrderCommand: CreateOrderCommand): CreateOrderResult {
        val order: Order = orderRepository.save(Order())
        val orderItems: List<OrderItem> = createOrderCommand.orderItems.map {
            OrderItem(
                orderId = order.id,
                productId = it.productId,
                quantity = it.quantity
            )
        }
        orderItemRepository.saveAll(orderItems)
        return CreateOrderResult(order.id)
    }

    fun placeOrder(
        placeOrderCommand: PlaceOrderCommand
    ) {
        val order: Order = orderRepository.findById(placeOrderCommand.orderId)
            .orElseThrow { IllegalArgumentException("주문정보를 찾을 수 없습니다.") }

        if (order.status == OrderStatus.COMPLETED) return


        var totalPrice: Long = 0L
        val orderItems: List<OrderItem> = orderItemRepository.findAllByOrderId(order.id)
        for (item in orderItems) {
            val price: Long = productService.buy(
                productId = item.productId,
                quantity = item.quantity
            )
            totalPrice += price
        }

        pointService.use(
            1L, totalPrice
        )
        order.complete() // dirty checking
    }
}
