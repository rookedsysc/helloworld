package com.rookedsysc.product.infrastructure.`in`

import com.rookedsysc.common.model.PageResponse
import com.rookedsysc.product.application.ProductCreateService
import com.rookedsysc.product.application.ProductListService
import com.rookedsysc.product.application.dto.ProductListResult
import com.rookedsysc.product.infrastructure.`in`.dto.ProductCreateRequest
import com.rookedsysc.product.infrastructure.`in`.dto.ProductListResponse
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@Tag(name = "상품 관리")
@RestController
@RequestMapping("/products")
class ProductController(
    private val productListService: ProductListService,
    private val productCreateService: ProductCreateService
) {
    @Operation(summary = "상품 목록 조회")
    @GetMapping
    fun list(
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "10") size: Int,
    ): PageResponse<ProductListResponse> {
        val pageable: Pageable = Pageable.ofSize(size).withPage(page)
        val productResults: Page<ProductListResult> = productListService.list(pageable)
        val productResponses: Page<ProductListResponse> = productResults.map { productResult ->
            ProductListResponse.from(productResult)
        }
        return PageResponse.from(productResponses)
    }

    @Operation(summary = "상품 생성")
    @PostMapping
    fun create(@RequestBody request: ProductCreateRequest): ProductListResponse {
        val productResult = productCreateService.create(request.toCommand())
        return ProductListResponse.from(productResult)
    }
}
