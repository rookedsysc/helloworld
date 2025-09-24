package com.rookedsysc.monolithic.point

import org.springframework.data.jpa.repository.JpaRepository

interface PointRepository : JpaRepository<Point, Long>{
    fun findByUserId(userId: Long): Point?
}
