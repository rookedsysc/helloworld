package com.rookedsysc.monolithic.init

import com.rookedsysc.monolithic.point.PointRepository
import com.rookedsysc.monolithic.product.Product
import com.rookedsysc.monolithic.product.ProductRepository
import jakarta.annotation.PostConstruct
import org.springframework.stereotype.Component

@Component
class TestDataCreator(
    private val pointRepository: PointRepository,
    private val productRepository: ProductRepository
) {

    @PostConstruct
    fun init() {
        if(pointRepository.countPointByUserId(userId = 1L) == 0L)  {
            pointRepository.save(
                com.rookedsysc.monolithic.point.Point(
                    userId = 1L,
                    amount = 10000L
                )
            )
        }

        productRepository.save(
            Product(
                quantity = 100L,
                price = 100L
            )
        )
        productRepository.save(
            Product(
                quantity = 200L,
                price = 200L
            )
        )
    }
}
