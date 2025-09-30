package com.rookedsysc.monolithic.config.lock

import java.util.concurrent.TimeUnit

/**
 * 락 설정을 위한 불변 데이터 클래스
 * Immutable Object Pattern
 */
data class DistributedLockConfig(
    val key: String,
    val waitTime: Long = 5,
    val leaseTime: Long = 10,
    val timeUnit: TimeUnit = TimeUnit.SECONDS,
    val fairLock: Boolean = true
) {}
