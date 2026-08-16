package com.rookedsysc.product.infrastructure.out

import com.rookedsysc.product.domain.ProductTransactionHistory
import org.springframework.data.jpa.repository.JpaRepository

interface ProductTransactionHistoryRepository : JpaRepository<ProductTransactionHistory, Long> {
    fun findAllByRequestIdAndTransactionType(
        requestId: String,
        transactionType: ProductTransactionHistory.TransactionType
    ): List<ProductTransactionHistory>

    fun findAllByRequestId(
        requestId: String,
    ): List<ProductTransactionHistory>
}
