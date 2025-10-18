package com.rookedsysc.product.domain

import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table

@Entity
@Table(name = "product_reservations")
class ProductReservation(
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    var id: Long = 0L,

    var requestId: String,

    var productId: Long,

    var reservedQuantity: Long,

    var reservedPrice: Long,

    @Enumerated(EnumType.STRING)
    var status: ProductReservationStatus = ProductReservationStatus.RESERVED
) {}
