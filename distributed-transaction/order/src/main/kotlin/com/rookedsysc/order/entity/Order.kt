package com.rookedsysc.order.entity

import jakarta.persistence.*

@Entity
@Table(name = "orders")
class Order(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long = 0L,

    var userId: Long,

    @Enumerated(EnumType.STRING)
    var status: OrderStatus = OrderStatus.CREATED
) {


    fun request() {
        if (status != OrderStatus.CREATED) {
            throw RuntimeException("잘못된 요청입니다.")
        }

        status = OrderStatus.REQUESTED
    }

    fun complete() {
        status = OrderStatus.COMPLETED
    }

    fun fail() {
        if (status != OrderStatus.REQUESTED) {
            throw RuntimeException("잘못된 요청입니다.")
        }
        status = OrderStatus.FAILED
    }

}
