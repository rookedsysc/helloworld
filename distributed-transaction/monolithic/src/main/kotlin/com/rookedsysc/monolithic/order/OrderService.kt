package com.rookedsysc.monolithic.order

import com.rookedsysc.monolithic.config.lock.DistributedLockConfig
import com.rookedsysc.monolithic.config.lock.FPDistributedLock
import jakarta.transaction.Transactional
import org.springframework.stereotype.Service

@Service
class OrderService(
    private val orderRepository: OrderRepository,
    private val orderItemRepository: OrderItemRepository,
    private val orderTransactionService: OrderTransactionService,
    private val fpLock: FPDistributedLock
) {

    @Transactional
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
            orderTransactionService.executeOrderTransaction(placeOrderCommand)
        }
    }


    private fun orderLockConfig(orderId: Long): DistributedLockConfig {
        return DistributedLockConfig(
            // Point 차감에서 동시성 발생하므로 userId로 차단이 맞음
            // userId는 hard coding 1L로 되있기 때문에 여기서도 하드코딩
            key = "lock:user:1",
            waitTime = 30,
            leaseTime = -1,
            timeUnit = java.util.concurrent.TimeUnit.SECONDS,
            fairLock = true
        )
    }
}
