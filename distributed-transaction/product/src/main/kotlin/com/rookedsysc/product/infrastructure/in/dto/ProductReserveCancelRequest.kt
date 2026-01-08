package com.rookedsysc.product.infrastructure.`in`.dto

import com.rookedsysc.product.application.dto.ProductReserveCancelCommand

data class ProductReserveCancelRequest(
    val requestId: String
) {
    fun toCommand(): ProductReserveCancelCommand {
        return ProductReserveCancelCommand(
            requestId = requestId
        )
    }
}
