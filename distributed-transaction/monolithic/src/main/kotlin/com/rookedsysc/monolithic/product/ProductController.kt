package com.rookedsysc.monolithic.product

import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController

@RestController
class ProductController(
    private val productService: ProductService
) {
    @GetMapping("/product")
    fun listProducts(): ProductListResponse {
        return productService.findALl()
    }
}
