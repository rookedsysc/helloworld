package com.rookedsysc.point.domain

import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table

@Entity
@Table(name = "points")
class Point(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long = 0L,

    var userId: Long,

    var amount: Long,

    var reservedAmount: Long = 0,
    ) {

    fun use(amount: Long) {
        if(amount < 0) {
            throw RuntimeException("사용할 포인트는 0보다 커야 합니다.")
        }
        if (this.amount < amount) {
            throw RuntimeException("포인트가 부족합니다.")
        }
        this.amount -= amount
    }

    /**
     * 포인트 예약 및 예약 금액 반환
     * throws RuntimeException 포인트 부족 시
     */
    fun reserve(requestedAmount: Long): Long {
        if(requestedAmount < 0) {
            throw RuntimeException("예약할 포인트는 0보다 커야 합니다.")
        }
        val reservableAmount = this.amount - this.reservedAmount
        if (reservableAmount < requestedAmount) {
            throw RuntimeException("포인트가 부족합니다.")
        }
        this.reservedAmount += requestedAmount
        return requestedAmount
    }

    fun confirm(requestedAmount: Long) {
        if (this.amount < requestedAmount || this.reservedAmount < requestedAmount) {
            throw RuntimeException("예약된 포인트가 부족합니다.")
        }
        this.reservedAmount -= requestedAmount
        this.amount -= requestedAmount
    }

    fun cancel(requestedAmount: Long) {
        if(this.reservedAmount < requestedAmount) {
            throw RuntimeException("예약된 포인트가 부족합니다.")
        }
        this.reservedAmount -= requestedAmount
    }
}
