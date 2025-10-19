package com.rookedsysc.point.domain

import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table

@Entity
@Table(name = "point_reservations")
class PointReservation(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long = 0L,

    var requestId: String,

    var pointId: Long,

    var reservedAmount: Long = 0L,

    var status: PointReservationStatus = PointReservationStatus.RESERVED
    ) {
}
