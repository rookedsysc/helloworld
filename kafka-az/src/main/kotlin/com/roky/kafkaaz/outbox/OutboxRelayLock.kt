package com.roky.kafkaaz.outbox

interface OutboxRelayLock {
    fun tryAcquire(): Boolean

    fun release()
}
