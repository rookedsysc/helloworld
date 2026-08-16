package com.rookedsysc.point.infrastructure.out

import com.rookedsysc.point.domain.PointTransactionHistory
import org.springframework.data.jpa.repository.JpaRepository

interface PointTransactionHistoryRepository: JpaRepository<PointTransactionHistory, Long> {
    fun findByRequestIdAndTransactionType(
        requestId: String,
        transactionType: PointTransactionHistory.TransactionType
    ): PointTransactionHistory?
}
