package com.rookedsysc.point.application

import com.rookedsysc.common.lock.DistributedLockWithTransaction
import com.rookedsysc.point.application.dto.PointUseCommand
import com.rookedsysc.point.domain.Point
import com.rookedsysc.point.domain.PointTransactionHistory
import com.rookedsysc.point.infrastructure.out.PointRepository
import com.rookedsysc.point.infrastructure.out.PointTransactionHistoryRepository
import org.springframework.stereotype.Service

@Service
class PointUseService(
    private val pointRepository: PointRepository,
    private val pointTransactionHistoryRepository: PointTransactionHistoryRepository
) {
    @DistributedLockWithTransaction(
        key = "product:orchestration:{command.requestId}",
        fairLock = true
    )
    fun use(command: PointUseCommand) {
        val pointTransactionHistory: PointTransactionHistory? =
            pointTransactionHistoryRepository.findByRequestIdAndTransactionType(
                requestId = command.requestId,
                transactionType = PointTransactionHistory.TransactionType.USE
            )

        if (pointTransactionHistory != null) throw RuntimeException("이미 사용한 이력이 존재합니다.")

        val point: Point = pointRepository.findByUserId(userId = command.userId)
            ?: let { throw RuntimeException("존재하지 않는 User ID 입니다.") }

        point.use(command.amount)
        pointTransactionHistoryRepository.save(
            PointTransactionHistory(
                requestId = command.requestId,
                pointId = point.id,
                amount = command.amount,
                transactionType = PointTransactionHistory.TransactionType.USE
            )
        )
    }

    fun cancel(command: PointUseCancelCommand) {
        val useHistory = pointTransactionHistoryRepository.findByRequestIdAndTransactionType(
            command.requestId(),
            PointTransactionHistory.TransactionType.USE
        )

        if (useHistory == null) {
            return
        }

        val cancelHistory = pointTransactionHistoryRepository.findByRequestIdAndTransactionType(
            command.requestId(),
            PointTransactionHistory.TransactionType.CANCEL
        )

        if (cancelHistory != null) {
            println("이미 취소된 요청입니다")
            return
        }

        val point = pointRepository.findById(useHistory.getPointId()).orElseThrow()

        point.cancel(useHistory.getAmount())
        pointTransactionHistoryRepository.save(
            PointTransactionHistory(
                command.requestId(),
                point.getId(),
                useHistory.getAmount(),
                PointTransactionHistory.TransactionType.CANCEL
            )
        )
    }

}
