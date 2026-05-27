package com.rookedsysc.order.application.event

data class OrderPlacedApplicationEvent(
    val orderId: Long,
    val userId: Long,
    val productInfos: List<ProductInfo>,
) {
    data class ProductInfo(
        val productId: Long,
        val quantity: Long,
    )
}
