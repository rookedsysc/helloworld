package com.rookedsysc.order.entity

import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table

@Entity
@Table(name = "orders")
class Order(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long = 0L,

    @Enumerated(EnumType.STRING)
    var status: OrderStatus = OrderStatus.CREATED
) {

    fun complete() {
        this.status = OrderStatus.COMPLETED
    }
}
