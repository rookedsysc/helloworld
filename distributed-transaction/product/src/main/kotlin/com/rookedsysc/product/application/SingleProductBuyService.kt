package com.rookedsysc.product.application

import com.rookedsysc.common.lock.DistributedLock
import com.rookedsysc.product.application.dto.SingleProductBuyCommand
import com.rookedsysc.product.application.dto.SingleProductBuyResult
import com.rookedsysc.product.application.dto.SingleProductCancelCommand
import com.rookedsysc.product.domain.Product
import com.rookedsysc.product.infrastructure.out.ProductRepository
import org.springframework.stereotype.Service

@Service
class SingleProductBuyService(
    private val productRepository: ProductRepository
) {
    @DistributedLock(
        key = "product:single-buy:{productId}",
        fairLock = true
    )
    fun buy(command: SingleProductBuyCommand): SingleProductBuyResult {

        val product: Product = productRepository.findById(command.productId).orElseThrow {
            RuntimeException("상품이 존재하지 않습니다.")
        }

        val price = product.calculatePrice(command.quantity)
        product.buy(command.quantity)
        return SingleProductBuyResult(
            price = price
        )
    }

    @DistributedLock(
        key = "product:single-buy:{productId}",
        fairLock = true
    )
    fun cancel(command: SingleProductCancelCommand) {
        val product: Product = productRepository.findById(command.productId).orElseThrow {
            RuntimeException("상품이 존재하지 않습니다.")
        }
        product.cancel(command.quantity)
    }
}
