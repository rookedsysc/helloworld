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

    var amount: Long
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
}
