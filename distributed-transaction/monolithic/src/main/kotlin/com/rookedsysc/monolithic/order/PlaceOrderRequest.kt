package com.rookedsysc.monolithic.order

class PlaceOrderRequest(
    val orderId: Long
) {
    fun toCommand(): PlaceOrderCommand {
        return PlaceOrderCommand(
            orderId = orderId
        )
    }
}
