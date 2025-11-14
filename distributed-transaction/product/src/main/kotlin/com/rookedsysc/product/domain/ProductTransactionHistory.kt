package com.rookedsysc.product.domain

import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table

/**
 * 중복 처리 방지 및 롤백 처리 용도
 */
@Entity
@Table(name = "proudct_transaction_histories")
class ProductTransactionHistory(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long = 0L,

    var requestId: String,

    var productId: Long,

    var quantity: Long,

    var price: Long,

    @Enumerated(EnumType.STRING)
    var transactionType: TransactionType
) {

    enum class TransactionType {
        PURCHASE, CANCEL
    }
}
