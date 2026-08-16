package com.rookedsysc.product.infrastructure.`in`.dto

import com.rookedsysc.product.application.dto.BunchProductBuyResult
import com.rookedsysc.product.application.dto.BunchProductCancelResult

data class ProductBuyResponse(
    val totalPrice: Long
) {
    companion object {
        fun of(result: BunchProductBuyResult): ProductBuyResponse {
            return ProductBuyResponse(
                totalPrice = result.totalPrice
            )
        }
    }
}
