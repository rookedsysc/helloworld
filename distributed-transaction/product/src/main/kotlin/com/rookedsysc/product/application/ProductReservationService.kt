package com.rookedsysc.product.application

import com.rookedsysc.common.lock.DistributedLockWithTransaction
import com.rookedsysc.product.application.dto.ProductReserveCancelCommand
import com.rookedsysc.product.application.dto.ProductReserveConfirmCommand
import com.rookedsysc.product.application.dto.ProductReserveCommand
import com.rookedsysc.product.application.dto.ProductReserveResult
import com.rookedsysc.product.domain.Product
import com.rookedsysc.product.domain.ProductReservation
import com.rookedsysc.product.domain.ProductReservationStatus
import com.rookedsysc.product.infrastructure.out.ProductRepository
import com.rookedsysc.product.infrastructure.out.ProductReservationRepository
import org.springframework.stereotype.Service

@Service
class ProductReservationService(
    private val productRepository: ProductRepository,
    private val productReservationRepository: ProductReservationRepository
) {
    companion object {
        private const val LOCK_KEY = "lock:product:reserve:{command.requestId}"
    }

    @DistributedLockWithTransaction(
        key = LOCK_KEY,
        fairLock = true
    )
    fun tryReserve(command: ProductReserveCommand) : ProductReserveResult {
        val exists: List<ProductReservation> = productReservationRepository.findAllByRequestId(command.requestId)

        if(!exists.isEmpty()) {
            val totalPrice: Long = exists.sumOf { it.reservedPrice }
            return ProductReserveResult(
                totalPrice = totalPrice
            )
        }

        var totalPrice: Long = 0L

        for(item in command.items) {
            val product: Product = productRepository.findById(item.productId).orElseThrow { RuntimeException("존재하지 않는 상품입니다.") }

            val price: Long = product.reserve(item.reserveQuantity)
            totalPrice += price

            productRepository.save(product)

            productReservationRepository.save(
                ProductReservation(
                    requestId =  command.requestId,
                    productId =  item.productId,
                    reservedQuantity =  item.reserveQuantity,
                    reservedPrice = price
                )
            )
        }

        return ProductReserveResult(totalPrice)
    }

    @DistributedLockWithTransaction(
        key = LOCK_KEY,
        fairLock = true
    )
    fun confirmReserve(command: ProductReserveConfirmCommand) {
        val reservations: List<ProductReservation> = productReservationRepository.findAllByRequestId(command.requestId)

        if (reservations.isEmpty()) {
            throw RuntimeException("예약 정보가 존재하지 않습니다.")
        }

        var alreadyReserved: Boolean = reservations.any { it.status == ProductReservationStatus.COMPLETED }

        if (alreadyReserved) {
            println("이미 예약이 완료된 요청입니다. requestId=${command.requestId}")
            return
        }

        for (reservation in reservations) {
            val product: Product = productRepository.findById(
                reservation
                    .productId
            ).orElseThrow { RuntimeException("존재하지 않는 상품입니다.") }

            product.confirm(reservation.reservedQuantity)
            reservation.confirm()

            productRepository.save(product)
            productReservationRepository.save(reservation)
        }
    }

    @DistributedLockWithTransaction(
        key = LOCK_KEY,
        fairLock = true
    )
    fun cancelReserve(command: ProductReserveCancelCommand) {
        val reservations: List<ProductReservation> = productReservationRepository.findAllByRequestId(command.requestId)

        if (reservations.isEmpty()) {
            throw RuntimeException("예약 정보가 존재하지 않습니다. requestId=${command.requestId}")
        }

        var alreadyCancelled: Boolean = reservations.any { it.status == ProductReservationStatus.CANCELED }
        var alreadyConfirmed: Boolean = reservations.any { it.status == ProductReservationStatus.COMPLETED }

        if (alreadyCancelled) {
            println("이미 예약이 취소된 요청입니다. requestId=${command.requestId}")
            return
        } else if(alreadyConfirmed) {
            throw RuntimeException("이미 확정된 예약은 취소할 수 없습니다. requestId=${command.requestId}")
        }

        for (reservation in reservations) {
            val product: Product = productRepository.findById(
                reservation
                    .productId
            ).orElseThrow { RuntimeException("존재하지 않는 상품입니다.") }

            product.cancel(reservation.reservedQuantity)
            reservation.cancel()

            productRepository.save(product)
            productReservationRepository.save(reservation)
        }
    }
}
