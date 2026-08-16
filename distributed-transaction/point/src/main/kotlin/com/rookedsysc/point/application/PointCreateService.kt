package com.rookedsysc.point.application

import com.rookedsysc.point.application.dto.PointCreateCommand
import com.rookedsysc.point.application.dto.PointListResult
import com.rookedsysc.point.domain.Point
import com.rookedsysc.point.infrastructure.out.PointRepository
import jakarta.transaction.Transactional
import org.springframework.stereotype.Service

@Service
class PointCreateService(
    private val pointRepository: PointRepository
) {
    @Transactional
    fun create(command: PointCreateCommand): PointListResult {
        val point = pointRepository.save(
            Point(
                userId = command.userId,
                amount = command.amount
            )
        )
        return PointListResult.from(point)
    }
}
