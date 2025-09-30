package com.rookedsysc.order.infrastructure.out

import com.rookedsysc.order.entity.Order
import org.springframework.data.jpa.repository.JpaRepository

interface OrderRepository : JpaRepository<Order, Long> {
}
