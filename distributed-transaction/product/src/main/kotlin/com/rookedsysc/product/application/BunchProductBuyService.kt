package com.rookedsysc.product.application

import com.rookedsysc.common.lock.DistributedLockWithTransaction
import com.rookedsysc.product.application.dto.BunchProductBuyCommand
import com.rookedsysc.product.application.dto.BunchProductBuyResult
import com.rookedsysc.product.application.dto.BunchProductCancelCommand
import com.rookedsysc.product.application.dto.BunchProductCancelResult
import com.rookedsysc.product.application.dto.SingleProductBuyCommand
import com.rookedsysc.product.application.dto.SingleProductBuyResult
import com.rookedsysc.product.application.dto.SingleProductCancelCommand
import com.rookedsysc.product.application.dto.SingleProductCancelResult
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

    @DistributedLockWithTransaction(
        key = "product:orchestration:{command.requestId}",
        fairLock = true
    )
    fun cancel(command: BunchProductCancelCommand): BunchProductCancelResult {
        val productTransactionHistories: List<ProductTransactionHistory> =
            productTransactionHistoryRepository.findAllByRequestId(
                requestId = command.requestId
            )
                .also {
                    if (it.isEmpty()) {
                        throw RuntimeException("주문 정보를 찾을 수 없습니다.")
                    }
                }

        var totalPrice = 0L

        // 취소된 주문건이면 로깅 후 반환
        if(productTransactionHistories.any { history -> history.transactionType == ProductTransactionHistory.TransactionType.CANCEL }) {
            println("이미 취소처리된 주문건 입니다.")
            productTransactionHistories.forEach { history ->
                if(history.transactionType == ProductTransactionHistory.TransactionType.CANCEL)  totalPrice += history.price
            }
            return BunchProductCancelResult(totalPrice = totalPrice)
        }

        for (history in productTransactionHistories) {
            singleProductBuyService.cancel(
                command = SingleProductCancelCommand(
                    productId = history.productId,
                    quantity = history.quantity
                )
            )
            totalPrice += history.price

            // 취소된 transaction 저장
            productTransactionHistoryRepository.save(
                ProductTransactionHistory(
                    requestId = history.requestId,
                    productId = history.productId,
                    quantity = history.quantity,
                    price = history.price,
                    transactionType = ProductTransactionHistory.TransactionType.CANCEL
                )
            )
        }

        return BunchProductCancelResult(
            totalPrice = totalPrice,
        )
    }
}
