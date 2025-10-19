package com.rookedsysc.point.domain

import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table

@Entity
@Table(name = "point_reservations")
class PointReservation(
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    var id: Long = 0L,

    var requestId: String,

    var pointId: Long,

    var reservedAmount: Long = 0L,

    @Enumerated(EnumType.STRING)
    var status: PointReservationStatus = PointReservationStatus.RESERVED
) {
    fun confirm() {
        if(this.status == PointReservationStatus.CANCELED) {
            throw RuntimeException("이미 취소된 예약 건입니다.")
        } else if(this.status == PointReservationStatus.COMPLETED) {
            throw RuntimeException("이미 완료된 예약 건입니다.")
        }

        this.status = PointReservationStatus.COMPLETED
    }

    fun cancel() {
        if(this.status == PointReservationStatus.CANCELED) {
            throw RuntimeException("이미 취소된 예약 건입니다.")
        } else if(this.status == PointReservationStatus.COMPLETED) {
            throw RuntimeException("이미 완료된 예약 건입니다.")
        }

        this.status = PointReservationStatus.CANCELED
    }
}
