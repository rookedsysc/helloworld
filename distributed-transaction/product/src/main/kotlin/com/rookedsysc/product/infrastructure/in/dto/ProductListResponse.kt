package com.rookedsysc.product.infrastructure.`in`.dto

import com.rookedsysc.product.application.dto.ProductListResult

data class ProductListResponse(
    val id: Long,
    val price: Long,
    val possibleQuantity: Long
) {
    companion object {
        fun from(
            productListResult: ProductListResult
        ): ProductListResponse {
            return ProductListResponse(
                id = productListResult.id,
                price = productListResult.price,
                possibleQuantity = productListResult.possibleQuantity
            )
        }
    }
}
