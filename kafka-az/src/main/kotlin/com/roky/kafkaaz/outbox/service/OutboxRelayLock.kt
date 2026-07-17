package com.roky.kafkaaz.outbox.service

interface OutboxRelayLock {
    fun tryAcquire(): Boolean

    fun release()
}
