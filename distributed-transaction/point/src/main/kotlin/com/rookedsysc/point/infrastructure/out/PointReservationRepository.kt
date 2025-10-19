package com.rookedsysc.point.infrastructure.out

import com.rookedsysc.point.domain.PointReservation
import org.springframework.data.jpa.repository.JpaRepository

interface PointReservationRepository: JpaRepository<PointReservation, Long> {
}
