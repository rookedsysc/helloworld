package com.rookedsysc.monolithic.order

import com.rookedsysc.monolithic.config.lock.DistributedLockConfig
import com.rookedsysc.monolithic.config.lock.FPDistributedLock
import com.rookedsysc.monolithic.point.PointService
import com.rookedsysc.monolithic.product.ProductService
import jakarta.transaction.Transactional
import org.springframework.stereotype.Service

@Service
class OrderService(
    private val orderRepository: OrderRepository,
    private val orderItemRepository: OrderItemRepository,
    private val orderTransactionService: OrderTransactionService,
    private val productService: ProductService,
    private val pointService: PointService,
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
            key = "lock:order:$orderId",
            waitTime = 5,
            leaseTime = 10,
            timeUnit = java.util.concurrent.TimeUnit.SECONDS,
            fairLock = true
        )
    }
}
