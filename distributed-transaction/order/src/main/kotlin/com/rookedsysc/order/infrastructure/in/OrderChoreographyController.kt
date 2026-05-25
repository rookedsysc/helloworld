package com.rookedsysc.order.infrastructure.`in`

import com.rookedsysc.order.application.OrderChoreographyService
import com.rookedsysc.order.infrastructure.`in`.dto.PlaceOrderRequest
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@Tag(name = "주문 Choreography API")
@RestController
@RequestMapping("orders/choreography")
class OrderChoreographyController(
    private val orderChoreographyService: OrderChoreographyService,
) {
    @Operation(summary = "주문 처리")
    @PostMapping("place")
    fun placeOrder(
        @RequestBody request: PlaceOrderRequest
    ) {
        return orderChoreographyService.placeOrder(command = request.toCommand())
    }
}
