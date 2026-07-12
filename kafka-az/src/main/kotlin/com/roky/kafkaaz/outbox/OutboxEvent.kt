package com.roky.kafkaaz.outbox

import java.time.Instant
import java.util.UUID
import org.jooq.JSONB

data class OutboxEvent(
    val id: UUID,
    val aggregateId: Long,
    val eventType: OutboxEventType,
    val payload: JSONB,
    val createdAt: Instant? = null,
    val publishedAt: Instant? = null
)
