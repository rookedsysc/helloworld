package com.rookedsysc.monolithic.point

import jakarta.transaction.Transactional
import org.springframework.stereotype.Service

@Service
class PointService(
    private val pointRepository: PointRepository
) {

    fun getPoint(): PointResponse {
        val point: Point = pointRepository.findByUserId(1L) ?: throw RuntimeException("포인트가 존재하지 않습니다.")
        return PointResponse(
            id = point.id,
            userId = point.userId,
            amount = point.amount
        )
    }

    @Transactional
    fun use(userId: Long, amount: Long): Unit {
        // userId로 포인트 조회 및 존재하지 않으면 예외 처리
        val point = pointRepository.findByUserId(userId) ?: throw RuntimeException("포인트가 존재하지 않습니다.")
        point.use(amount)
        // JPA dirty checking이 자동으로 변경사항을 저장함 - explicit save() 제거
    }
}
