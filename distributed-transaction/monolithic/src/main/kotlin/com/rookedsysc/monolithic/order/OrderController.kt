package com.rookedsysc.monolithic.order

import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController

@RestController
class OrderController(
    private val orderService: OrderService
) {
    @PostMapping("/order")
    fun createOrder(
        @RequestBody request: CreateOrderRequest
    ): CreateOrderResponse {
        val createOrderResult: CreateOrderResult = orderService.createOrder(request.toCommand())
        return CreateOrderResponse(createOrderResult.orderId)
    }

    @PostMapping("/order/place")
    fun placeOrder(
        @RequestBody request: PlaceOrderRequest
    ) {
        orderService.placeOrderWithLock(request.toCommand())
    }
}
