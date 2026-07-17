package com.roky.kafkaaz.outbox.service

import kotlin.test.assertFalse
import kotlin.test.assertTrue
import org.junit.jupiter.api.Test

class AtomicBooleanOutboxRelayLockTest {

    @Test
    fun `allows another relay run after the current run releases the lock`() {
        val lock = AtomicBooleanOutboxRelayLock()

        assertTrue(lock.tryAcquire())
        assertFalse(lock.tryAcquire())

        lock.release()

        assertTrue(lock.tryAcquire())
    }
}
