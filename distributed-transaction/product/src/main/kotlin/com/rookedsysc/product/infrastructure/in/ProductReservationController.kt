package com.rookedsysc.product.infrastructure.`in`

import com.rookedsysc.product.application.ProductService
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
@RequestMapping("/products")
class ProductReservationController(
    private val productService: ProductService,
) {
    @Operation(summary = "상품 예약 생성")
    @PostMapping("/reserve")
    fun reserve(@RequestBody request: ProductReserveRequest): ProductReserveResponse {
        val command = request.toCommand()
        val totalPrice = productService.tryReserve(command).totalPrice
        return ProductReserveResponse(totalPrice = totalPrice)
    }
}
