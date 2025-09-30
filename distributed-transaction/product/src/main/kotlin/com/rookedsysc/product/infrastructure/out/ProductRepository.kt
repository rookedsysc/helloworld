package com.rookedsysc.product.infrastructure.out

import com.rookedsysc.product.domain.Product
import org.springframework.data.jpa.repository.JpaRepository

interface ProductRepository : JpaRepository<Product, Long> {
}
