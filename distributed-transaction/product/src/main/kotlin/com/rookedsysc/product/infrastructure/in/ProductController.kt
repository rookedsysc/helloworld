package com.rookedsysc.product.infrastructure.`in`

import com.rookedsysc.product.application.ProductService
import com.rookedsysc.product.infrastructure.`in`.dto.ProductReserveRequest
import com.rookedsysc.product.infrastructure.`in`.dto.ProductReserveResponse
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController

@RestController
class ProductController(
    private val productService: ProductService
) {

    @PostMapping("/products/reserve")
    fun reserve(@RequestBody request: ProductReserveRequest): ProductReserveResponse {
        val command = request.toCommand()
        val totalPrice = productService.tryReserve(command).totalPrice
        return ProductReserveResponse(totalPrice = totalPrice)
    }
}
