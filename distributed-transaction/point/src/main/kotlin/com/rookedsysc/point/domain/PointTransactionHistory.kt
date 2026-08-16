package com.rookedsysc.point.domain

import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table

@Entity
@Table(name = "point_transaction_histories")
class PointTransactionHistory(

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long = 0L,

    var requestId: String,

    var pointId: Long,

    var amount: Long,

    @Enumerated(EnumType.STRING)
    var transactionType: TransactionType
) {
    enum class TransactionType {
        USE, CANCEL
    }
}
