package com.rookedsysc.monolithic.order

import com.rookedsysc.monolithic.point.PointService
import com.rookedsysc.monolithic.product.ProductService
import jakarta.transaction.Transactional
import org.springframework.stereotype.Service

@Service
class OrderTransactionService(
    private val orderRepository: OrderRepository,
    private val orderItemRepository: OrderItemRepository,
    private val productService: ProductService,
    private val pointService: PointService
) {

    /**
     * 트랜잭션 내에서 주문 처리를 수행하는 메서드
     * 분산 락이 걸린 상태에서 호출됨
     */
    @Transactional
    fun executeOrderTransaction(placeOrderCommand: PlaceOrderCommand) {
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

        pointService.use(1L, totalPrice)
        order.complete()
        orderRepository.save(order)
    }
}
