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
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@Tag(name = "주문 API")
@RestController
@RequestMapping("orders")
class OrderController(
    private val orderService: OrderService, private val orderCoordinator: OrderCoordinator
) {

    @Operation(summary = "주문 생성")
    @PostMapping
    fun createOrder(
        @RequestBody request: CreateOrderRequest
    ): CreateOrderResponse {
        val result: CreateOrderResult = orderService.createOrder(command = request.toCommand())
        return CreateOrderResponse.of(result)
    }

    @Operation(summary = "주문 처리")
    @PostMapping("place")
    fun placeOrder(
        @RequestBody request: PlaceOrderRequest
    ) {
        return orderCoordinator.placeOrder(command = request.toCommand())
    }
}
