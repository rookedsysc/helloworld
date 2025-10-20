package com.rookedsysc.order.infrastructure.`in`

import com.rookedsysc.order.application.OrderCoordinator
import com.rookedsysc.order.application.OrderService
import com.rookedsysc.order.application.dto.CreateOrderResult
import com.rookedsysc.order.infrastructure.`in`.dto.CreateOrderRequest
import com.rookedsysc.order.infrastructure.`in`.dto.CreateOrderResponse
import com.rookedsysc.order.infrastructure.`in`.dto.PlaceOrderRequest
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController

@Tag(name = "상품 주문")
@RestController
class OrderController(
    private val orderService: OrderService,
    private val orderCoordinator: OrderCoordinator
) {
    @Operation(summary = "주문 생성")
    @PostMapping("/order")
    fun createOrder(
        @RequestBody request: CreateOrderRequest
    ): CreateOrderResponse {
        val createOrderResult: CreateOrderResult = orderService.createOrder(request.toCommand())
        return CreateOrderResponse(createOrderResult.orderId)
    }

    @Operation(summary = "주문 확정")
    @PostMapping("/order/place")
    fun placeOrder(
        @RequestBody request: PlaceOrderRequest
    ) {
        orderCoordinator.placeOrder(request.toCommand())
    }
}
