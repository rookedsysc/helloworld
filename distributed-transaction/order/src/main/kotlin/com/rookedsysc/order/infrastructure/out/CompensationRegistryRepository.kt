package com.rookedsysc.order.infrastructure.out

import com.rookedsysc.order.entity.CompensationRegistry
import org.springframework.data.jpa.repository.JpaRepository

interface CompensationRegistryRepository: JpaRepository<CompensationRegistry, Long> {
}
