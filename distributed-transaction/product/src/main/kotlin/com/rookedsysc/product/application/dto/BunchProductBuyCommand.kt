package com.rookedsysc.product.application.dto

data class BunchProductBuyCommand(
    val requestId: String,
    val productInfos: List<ProductInfo>,
) {
    data class ProductInfo(
        val productId: Long,
        val quantity: Long
    )
}
