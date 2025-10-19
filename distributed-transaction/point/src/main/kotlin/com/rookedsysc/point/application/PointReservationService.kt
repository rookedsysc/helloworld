package com.rookedsysc.point.application

import com.rookedsysc.common.lock.DistributedLockWithTransaction
import com.rookedsysc.point.application.dto.PointReserveCancelCommand
import com.rookedsysc.point.application.dto.PointReserveCommand
import com.rookedsysc.point.application.dto.PointReserveConfirmCommand
import com.rookedsysc.point.application.dto.PointReserveResult
import com.rookedsysc.point.domain.Point
import com.rookedsysc.point.domain.PointReservation
import com.rookedsysc.point.domain.PointReservationStatus
import com.rookedsysc.point.infrastructure.out.PointRepository
import com.rookedsysc.point.infrastructure.out.PointReservationRepository
import org.springframework.stereotype.Service

@Service
class PointReservationService(
    private val pointRepository: PointRepository,
    private val pointReservationRepository: PointReservationRepository
) {
    companion object {
        private const val LOCK_KEY = "lock:point:reserve:{command.requestId}"
    }

    @DistributedLockWithTransaction(
        key = LOCK_KEY,
        fairLock = true
    )
    fun tryReserve(command: PointReserveCommand): PointReserveResult {
        val exists: List<PointReservation> = pointReservationRepository.findAllByRequestId(command.requestId)

        if (!exists.isEmpty()) {
            val totalAmount: Long = exists.sumOf { it.reservedAmount }
            return PointReserveResult(
                reservedAmount = totalAmount
            )
        }

        val point: Point = pointRepository.findByUserId(command.userId)
            ?: throw RuntimeException("사용자의 포인트 정보가 존재하지 않습니다.")

        val reservedAmount: Long = point.reserve(command.amount)

        pointRepository.save(point)

        pointReservationRepository.save(
            PointReservation(
                requestId = command.requestId,
                pointId = point.id,
                reservedAmount = reservedAmount
            )
        )

        return PointReserveResult(reservedAmount)
    }

    @DistributedLockWithTransaction(
        key = LOCK_KEY,
        fairLock = true
    )
    fun confirmReserve(command: PointReserveConfirmCommand) {
        val reservations: List<PointReservation> = pointReservationRepository.findAllByRequestId(command.requestId)

        if (reservations.isEmpty()) {
            throw RuntimeException("예약 정보가 존재하지 않습니다.")
        }

        val alreadyReserved: Boolean = reservations.any { it.status == PointReservationStatus.COMPLETED }

        if (alreadyReserved) {
            println("이미 예약이 완료된 요청입니다. requestId=${command.requestId}")
            return
        }

        for (reservation in reservations) {
            val point: Point = pointRepository.findById(
                reservation.pointId
            ).orElseThrow { RuntimeException("포인트 정보가 존재하지 않습니다.") }

            point.confirm(reservation.reservedAmount)
            reservation.confirm()

            pointRepository.save(point)
            pointReservationRepository.save(reservation)
        }
    }

    @DistributedLockWithTransaction(
        key = LOCK_KEY,
        fairLock = true
    )
    fun cancelReserve(command: PointReserveCancelCommand) {
        val reservations: List<PointReservation> = pointReservationRepository.findAllByRequestId(command.requestId)

        if (reservations.isEmpty()) {
            throw RuntimeException("예약 정보가 존재하지 않습니다. requestId=${command.requestId}")
        }

        val alreadyCancelled: Boolean = reservations.any { it.status == PointReservationStatus.CANCELED }
        val alreadyConfirmed: Boolean = reservations.any { it.status == PointReservationStatus.COMPLETED }

        if (alreadyCancelled) {
            println("이미 예약이 취소된 요청입니다. requestId=${command.requestId}")
            return
        } else if (alreadyConfirmed) {
            throw RuntimeException("이미 확정된 예약은 취소할 수 없습니다. requestId=${command.requestId}")
        }

        for (reservation in reservations) {
            val point: Point = pointRepository.findById(
                reservation.pointId
            ).orElseThrow { RuntimeException("포인트 정보가 존재하지 않습니다.") }

            point.cancel(reservation.reservedAmount)
            reservation.cancel()

            pointRepository.save(point)
            pointReservationRepository.save(reservation)
        }
    }
}
