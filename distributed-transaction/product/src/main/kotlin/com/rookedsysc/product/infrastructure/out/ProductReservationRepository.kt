package com.rookedsysc.product.infrastructure.out

import com.rookedsysc.product.domain.ProductReservation
import org.springframework.data.jpa.repository.JpaRepository

interface ProductReservationRepository: JpaRepository<ProductReservation, Long> {
}
