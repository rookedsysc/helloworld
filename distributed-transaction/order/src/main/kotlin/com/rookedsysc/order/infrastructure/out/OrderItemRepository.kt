package com.rookedsysc.order.infrastructure.out

import com.rookedsysc.order.domain.OrderItem
import org.springframework.data.jpa.repository.JpaRepository

interface OrderItemRepository: JpaRepository<OrderItem, Long> {
    fun findAllByOrderId(orderId: Long): List<OrderItem>
}
