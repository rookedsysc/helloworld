package com.rookedsysc.product.infrastructure.`in`.dto

import com.rookedsysc.product.application.dto.ProductReserveCommand

data class ProductReserveRequest(
    val requestId: String,
    val items: List<ReserveItem>
) {
    data class ReserveItem(
        val productId: Long,
        val reserveQuantity: Long
    )

    fun toCommand(): ProductReserveCommand {
         return ProductReserveCommand(
             requestId = requestId,
             items = items.map {
                 ProductReserveCommand.ReserveItem(
                     productId = it.productId,
                     reserveQuantity = it.reserveQuantity
                 )
             }.toList()
         )
    }
}
