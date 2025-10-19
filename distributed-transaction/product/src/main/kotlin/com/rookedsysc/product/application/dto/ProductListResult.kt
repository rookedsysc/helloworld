package com.rookedsysc.product.application.dto

import com.rookedsysc.product.domain.Product

data class ProductListResult(
    val id: Long,
    val price: Long,
    val possibleQuantity: Long
) {
    companion object {
        fun from(product: Product): ProductListResult {
            return ProductListResult(
                id = product.id,
                price = product.price,
                possibleQuantity = product.quantity - product.reservedQuantity
            )
        }
    }
}
