package com.rookedsysc.order.entity

import jakarta.persistence.*

/**
 * Cancel 실패한 경우에 실패 정보 저장
 */
@Entity
@Table(name = "compensation_registries")
class CompensationRegistry(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long = 0L,
    val orderId: Long, // 이건 final이어야 할 것 같은데
    @Enumerated(EnumType.STRING)
    var status: CompensationRegistryStatus = CompensationRegistryStatus.PENDING
) {

    enum class CompensationRegistryStatus {
        PENDING, COMPLETE
    }
}
