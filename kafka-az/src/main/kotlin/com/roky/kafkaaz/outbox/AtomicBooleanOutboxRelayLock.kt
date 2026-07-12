package com.roky.kafkaaz.outbox

import java.util.concurrent.atomic.AtomicBoolean
import org.springframework.stereotype.Component

@Component
class AtomicBooleanOutboxRelayLock : OutboxRelayLock {
    private val acquired = AtomicBoolean(false)

    override fun tryAcquire(): Boolean {
        return acquired.compareAndSet(false, true)
    }

    override fun release() {
        acquired.set(false)
    }
}
