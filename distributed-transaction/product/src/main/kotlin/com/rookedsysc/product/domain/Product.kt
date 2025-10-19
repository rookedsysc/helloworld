package com.rookedsysc.product.domain

import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table

@Entity
@Table(name = "products")
class Product(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long = 0L,

    var quantity: Long,

    var price: Long,

    var reservedQuantity: Long = 0L,
) {

    fun calculatePrice(quantity: Long): Long {
        if (this.quantity < quantity) {
            throw RuntimeException("재고가 부족합니다.")
        }
        if (this.quantity < 1) {
            throw RuntimeException("0개 이하로 구매할 수 없습니다.")
        }
        return this.price * quantity
    }

    fun buy(quantity: Long) {
        if (this.quantity < quantity) {
            throw RuntimeException("재고가 부족합니다.")
        }
        this.quantity -= quantity
    }

    fun confirm(reservedQuantity: Long) {
        if (this.quantity < reservedQuantity || this.reservedQuantity < reservedQuantity) {
            throw RuntimeException("예약된 재고가 부족합니다.")
        }
        this.reservedQuantity -= reservedQuantity
        this.quantity -= reservedQuantity
    }

    /**
     * 재고 예약 및 예약 금액 반환
     * throws RuntimeException 재고 부족 시
     */
    fun reserve(requestedQuantity: Long): Long {
        val reservableQuantity = this.quantity + this.reservedQuantity
        if (reservableQuantity < requestedQuantity) {
            throw RuntimeException("재고가 부족합니다.")
        }
        this.reservedQuantity += requestedQuantity
        return this.price * requestedQuantity
    }
}
