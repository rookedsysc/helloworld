package com.rookedsysc.point.application

import com.rookedsysc.point.application.dto.PointListResult
import com.rookedsysc.point.domain.Point
import com.rookedsysc.point.infrastructure.out.PointRepository
import jakarta.transaction.Transactional
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service

@Service
class PointListService(
    private val pointRepository: PointRepository
) {
    @Transactional
    fun list(pageable: Pageable): Page<PointListResult> {
        val points: Page<Point> = pointRepository.findAll(pageable)
        return points.map { point -> PointListResult.from(point) }
    }
}
