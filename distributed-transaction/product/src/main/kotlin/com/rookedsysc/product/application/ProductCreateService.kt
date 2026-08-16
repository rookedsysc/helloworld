package com.rookedsysc.product.application

import com.rookedsysc.product.application.dto.ProductCreateCommand
import com.rookedsysc.product.application.dto.ProductListResult
import com.rookedsysc.product.domain.Product
import com.rookedsysc.product.infrastructure.out.ProductRepository
import jakarta.transaction.Transactional
import org.springframework.stereotype.Service

@Service
class ProductCreateService(
    private val productRepository: ProductRepository
) {
    @Transactional
    fun create(command: ProductCreateCommand): ProductListResult {
        val product = productRepository.save(
            Product(
                quantity = command.quantity,
                price = command.price,
            )
        )
        return ProductListResult.from(product)
    }
}
