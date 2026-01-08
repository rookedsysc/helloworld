package com.rookedsysc.product.infrastructure.`in`

import com.rookedsysc.product.application.ProductReservationService
import com.rookedsysc.product.infrastructure.`in`.dto.ProductReserveCancelRequest
import com.rookedsysc.product.infrastructure.`in`.dto.ProductReserveConfirmRequest
import com.rookedsysc.product.infrastructure.`in`.dto.ProductReserveRequest
import com.rookedsysc.product.infrastructure.`in`.dto.ProductReserveResponse
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@Tag(name = "상품 예약")
@RestController
@RequestMapping("/products/reservations")
class ProductReservationController(
    private val productReservationService: ProductReservationService,
) {
    @Operation(summary = "상품 예약 생성")
    @PostMapping
    fun reserve(@RequestBody request: ProductReserveRequest): ProductReserveResponse {
        val command = request.toCommand()
        val totalPrice = productReservationService.tryReserve(command).totalPrice
        return ProductReserveResponse(totalPrice = totalPrice)
    }

    @Operation(summary = "상품 예약 확정")
    @PostMapping("/confirm")
    fun confirm(
        @RequestBody request: ProductReserveConfirmRequest
    ) {
        val command = request.toCommand()
        productReservationService.confirmReserve(command)
    }

    @Operation(summary = "상품 예약 취소")
    @PostMapping("/cancel")
    fun cancel(@RequestBody request: ProductReserveCancelRequest) {
        return productReservationService.cancelReserve(command = request.toCommand())
    }

}
