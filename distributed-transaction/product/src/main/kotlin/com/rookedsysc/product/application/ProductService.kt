package com.rookedsysc.product.application

import com.rookedsysc.common.lock.DistributedLockWithTransaction
import com.rookedsysc.product.application.dto.ProductReserveCommand
import com.rookedsysc.product.application.dto.ProductReserveResult
import com.rookedsysc.product.domain.Product
import com.rookedsysc.product.domain.ProductReservation
import com.rookedsysc.product.infrastructure.out.ProductRepository
import com.rookedsysc.product.infrastructure.out.ProductReservationRepository
import org.springframework.stereotype.Service

@Service
class ProductService(
    private val productRepository: ProductRepository,
    private val productReservationRepository: ProductReservationRepository
) {

    @DistributedLockWithTransaction(
        key = "lock:product:reserve:{command.requestId}",
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
}
