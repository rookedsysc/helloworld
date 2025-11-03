package com.rookedsysc.product.infrastructure.`in`

import com.rookedsysc.product.application.BunchProductBuyService
import com.rookedsysc.product.application.dto.ProductCancelRequest
import com.rookedsysc.product.application.dto.ProductCancelResponse
import com.rookedsysc.product.infrastructure.`in`.dto.ProductBuyRequest
import com.rookedsysc.product.infrastructure.`in`.dto.ProductBuyResponse
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@Tag(name = "상품 API")
@RestController
@RequestMapping("products")
class ProductController(
    private val productService: BunchProductBuyService
) {

    @Operation(summary = "상품 구매")
    @PostMapping("buy")
    fun buy(@RequestBody request: ProductBuyRequest): ProductBuyResponse {
        val result = productService.buy(request.toCommand())
        return ProductBuyResponse.of(result = result)
    }

    @Operation(summary = "상품 취소")
    @PostMapping("cancel")
    fun cancel(@RequestBody request: ProductCancelRequest): ProductCancelResponse{
        val result = productService.cancel(request.toCommand())
        return ProductCancelResponse.of(result = result)
    }
}
