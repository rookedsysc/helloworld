package com.rookedsysc.product.infrastructure.`in`.dto

import com.rookedsysc.product.application.dto.ProductCreateCommand

data class ProductCreateRequest(
    val quantity: Long,
    val price: Long,
) {
    fun toCommand() =
        ProductCreateCommand(
            quantity = this.quantity,
            price = this.price,
        )
}
