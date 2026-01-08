package com.rookedsysc.order.domain

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

    var userId: Long,

    @Enumerated(EnumType.STRING)
    var status: OrderStatus = OrderStatus.CREATED
) {

    fun confirm() {
        if(this.status != OrderStatus.RESERVED && this.status != OrderStatus.PENDING) {
            throw RuntimeException("예약 단계 혹은 Pending 단계에서만 완료할 수 있습니다.")
        }

        this.status = OrderStatus.COMPLETED
    }

    fun pending() {
        if(this.status != OrderStatus.RESERVED) {
            throw RuntimeException("예약된 단계에서만 보류할 수 있습니다.")
        }

        this.status = OrderStatus.PENDING
    }

    fun reserve() {
        if(this.status != OrderStatus.CREATED) {
            throw RuntimeException("생성된 단계에서만 예약할 수 있습니다.")
        }

        this.status = OrderStatus.RESERVED
    }

    fun cancel() {
        if(this.status != OrderStatus.RESERVED) {
            throw RuntimeException("예약된 단계에서만 취소할 수 있습니다.")
        }

        this.status = OrderStatus.CANCELED
    }
}
