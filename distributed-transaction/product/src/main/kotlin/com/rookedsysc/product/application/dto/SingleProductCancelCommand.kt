package com.rookedsysc.product.application.dto

data class SingleProductCancelCommand(
    val productId: Long,
    val quantity: Long
) {
}
