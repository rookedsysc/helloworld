package com.rookedsysc.monolithic.order

import com.rookedsysc.monolithic.config.lock.DistributedLockConfig
import com.rookedsysc.monolithic.config.lock.FPDistributedLock
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
    private val fpLock: FPDistributedLock
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

    fun placeOrderWithLock(placeOrderCommand: PlaceOrderCommand) {
        fpLock.withLock(orderLockConfig(placeOrderCommand.orderId)) {
            placeOrder(placeOrderCommand)
        }
    }

    /**
     * 주문 처리 메서드
     * DistributedLock을 사용하여 동시성 제어
     * LockKeyConstants의 getOrderLockKey 메서드를 SpEL로 호출하여 락 키 생성
     */
    private fun placeOrder(
        placeOrderCommand: PlaceOrderCommand
    ) {
        val order: Order = orderRepository.findById(placeOrderCommand.orderId)
            .orElseThrow { IllegalArgumentException("주문정보를 찾을 수 없습니다.") }

        if (order.status == OrderStatus.COMPLETED) return

        var totalPrice = 0L
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
        order.complete()
        orderRepository.save(order)
    }

    private fun orderLockConfig(orderId: Long): DistributedLockConfig {
        return DistributedLockConfig(
            key = "lock:order:$orderId",
            waitTime = 5,
            leaseTime = 10,
            timeUnit = java.util.concurrent.TimeUnit.SECONDS,
            fairLock = true
        )
    }
}
