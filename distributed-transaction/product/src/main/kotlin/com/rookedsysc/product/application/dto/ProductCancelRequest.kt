package com.rookedsysc.product.application.dto

data class ProductCancelRequest(
    val requestId: String,
) {

    fun toCommand(): BunchProductCancelCommand {
        return BunchProductCancelCommand(
            requestId = requestId,
        )
    }
}
