package com.rookedsysc.product.application

import com.rookedsysc.product.application.dto.ProductListResult
import com.rookedsysc.product.domain.Product
import com.rookedsysc.product.infrastructure.out.ProductRepository
import jakarta.transaction.Transactional
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service

@Service
class ProductListService(
    private val productRepository: ProductRepository
) {
    @Transactional
    fun list(pageable: Pageable): Page<ProductListResult> {
        val products: Page<Product> = productRepository.findAll(pageable)
        return products.map { product -> ProductListResult.from(product) }
    }
}
