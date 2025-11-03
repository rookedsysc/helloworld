package com.rookedsysc.product.application

import com.rookedsysc.common.lock.DistributedLockWithTransaction
import com.rookedsysc.product.application.dto.BunchProductBuyCommand
import com.rookedsysc.product.application.dto.BunchProductBuyResult
import com.rookedsysc.product.application.dto.SingleProductBuyCommand
import com.rookedsysc.product.application.dto.SingleProductBuyResult
import com.rookedsysc.product.domain.ProductTransactionHistory
import com.rookedsysc.product.infrastructure.out.ProductTransactionHistoryRepository
import org.springframework.stereotype.Service

@Service
class BunchProductBuyService(
    private val singleProductBuyService: SingleProductBuyService,
    private val productTransactionHistoryRepository: ProductTransactionHistoryRepository,
) {
    @DistributedLockWithTransaction(
        key = "product:orchestration:{command.requestId}",
        fairLock = true
    )
    fun buy(command: BunchProductBuyCommand): BunchProductBuyResult {
        productTransactionHistoryRepository.findAllByRequestIdAndTransactionType(
            requestId = command.requestId,
            transactionType = ProductTransactionHistory.TransactionType.PURCHASE
        )
            .also {
                if (it.isNotEmpty()) {
                    throw RuntimeException("이미 처리된 요청입니다.")
                }
            }

        var totalPrice: Long = 0L

        for (productInfo in command.productInfos) {
            val singleProductBuyResult: SingleProductBuyResult = singleProductBuyService.buy(
                command = SingleProductBuyCommand(
                    productId = productInfo.productId,
                    quantity = productInfo.quantity
                )
            )
            totalPrice += singleProductBuyResult.price

            productTransactionHistoryRepository.save(
                ProductTransactionHistory(
                    requestId = command.requestId,
                    productId = productInfo.productId,
                    quantity = productInfo.quantity,
                    price = singleProductBuyResult.price,
                    transactionType = ProductTransactionHistory.TransactionType.PURCHASE
                )
            )
        }

        return BunchProductBuyResult(totalPrice = totalPrice)
    }
}
