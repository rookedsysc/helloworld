package com.rookedsysc.product.domain

import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import org.junit.jupiter.api.Test

class ProductTest {

    @Test
    fun `예약 가능 수량은 남은 재고에서 이미 선점된 수량을 뺀 만큼이다`() {
        val product = Product(quantity = 5L, price = 1_000L)

        product.reserve(3L)

        assertFailsWith<RuntimeException> { product.reserve(3L) }
        assertEquals(3L, product.reservedQuantity)
    }

    @Test
    fun `재고 1개는 예약 1건까지만 선점할 수 있다`() {
        val product = Product(quantity = 1L, price = 1_000L)

        product.reserve(1L)

        repeat(4) {
            assertFailsWith<RuntimeException> { product.reserve(1L) }
        }
        assertEquals(1L, product.reservedQuantity)
    }

    @Test
    fun `선점한 예약을 확정하면 재고와 예약 수량이 함께 줄어든다`() {
        val product = Product(quantity = 5L, price = 1_000L)
        val reservedPrice = product.reserve(2L)

        product.confirm(2L)

        assertEquals(2_000L, reservedPrice)
        assertEquals(3L, product.quantity)
        assertEquals(0L, product.reservedQuantity)
    }

    @Test
    fun `선점한 예약을 취소하면 재고는 그대로이고 예약 수량만 풀린다`() {
        val product = Product(quantity = 5L, price = 1_000L)
        product.reserve(2L)

        product.cancel(2L)

        assertEquals(5L, product.quantity)
        assertEquals(0L, product.reservedQuantity)
    }
}
