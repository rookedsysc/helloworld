package com.rookedsysc.product.application.dto

data class ProductCancelResponse(
    val totalPrice: Long
) {
    companion object {
        fun of(result: BunchProductCancelResult): ProductCancelResponse {
            return ProductCancelResponse(
                totalPrice = result.totalPrice
            )
        }
    }
}
