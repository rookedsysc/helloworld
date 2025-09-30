package com.rookedsysc.point.infrastructure.out

import com.rookedsysc.point.entity.Point
import org.springframework.data.jpa.repository.JpaRepository

interface PointRepository : JpaRepository<Point, Long> {
    fun findByUserId(userId: Long): Point?

    fun countPointByUserId(userId: Long): Long
}
