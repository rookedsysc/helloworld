package com.rookedsysc.product.infrastructure.`in`.dto

import com.rookedsysc.product.application.dto.ProductListResult

data class ProductListResponse(
    val id: Long,
    val price: Long,
    val quantity: Long
) {
    companion object {
        fun of(
            productListResult: ProductListResult
        ): ProductListResponse {
            return ProductListResponse(
                id = productListResult.id,
                price = productListResult.price,
                quantity = productListResult.quantity
            )
        }
    }
}
