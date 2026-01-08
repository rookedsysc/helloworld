package com.rookedsysc.product.infrastructure.`in`.dto

import com.rookedsysc.product.application.dto.ProductReserveConfirmCommand

data class ProductReserveConfirmRequest(
    val requestId: String
) {
    fun toCommand(): ProductReserveConfirmCommand{
        return ProductReserveConfirmCommand(requestId = requestId)
    }
}
