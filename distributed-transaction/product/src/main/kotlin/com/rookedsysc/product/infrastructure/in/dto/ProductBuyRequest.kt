package com.rookedsysc.product.infrastructure.`in`.dto

import com.rookedsysc.product.application.dto.BunchProductBuyCommand

data class ProductBuyRequest(
    val requestId: String,
    val productInfos: List<ProductInfo>,
    ) {

    data class ProductInfo(
        val productId: Long, val quantity: Long
    )

    fun toCommand(): BunchProductBuyCommand {
        return BunchProductBuyCommand(
            requestId = this.requestId, productInfos = this.productInfos.map {
                BunchProductBuyCommand.ProductInfo(
                    productId = it.productId, quantity = it.quantity
                )
            })
    }
}
