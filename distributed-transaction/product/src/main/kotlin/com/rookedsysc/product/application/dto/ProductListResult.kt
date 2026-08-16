package com.rookedsysc.product.application.dto

import com.rookedsysc.product.domain.Product

data class ProductListResult(
    val id: Long,
    val price: Long,
    val quantity: Long
) {
    companion object {
        fun from(product: Product): ProductListResult {
            return ProductListResult(
                id = product.id,
                price = product.price,
                quantity = product.quantity
            )
        }
    }
}
