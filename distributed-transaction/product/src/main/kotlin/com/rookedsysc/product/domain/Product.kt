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

    var price: Long
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
}
