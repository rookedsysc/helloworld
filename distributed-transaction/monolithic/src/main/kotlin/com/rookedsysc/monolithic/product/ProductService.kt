package com.rookedsysc.monolithic.product

import jakarta.transaction.Transactional
import org.springframework.stereotype.Service

@Service
class ProductService(
    private val productRepository: ProductRepository
) {
    fun findALl(): ProductListResponse {
        val productResponses: List<ProductResponse> = productRepository.findAll().map {
            ProductResponse(
                id = it.id, quantity = it.quantity, price = it.price
            )
        }
        return ProductListResponse(productResponses)
    }

    // 개수를 받아서 구매하고 총 가격을 반환
    @Transactional
    fun buy(productId: Long, quantity: Long): Long {
        val product = productRepository.findById(productId).orElseThrow { RuntimeException("Product not found") }

        val totalPrice: Long = product.calculatePrice(quantity)
        product.buy(quantity)

        // JPA dirty checking이 자동으로 변경사항을 저장함 - explicit save() 제거
        return totalPrice
    }
}
