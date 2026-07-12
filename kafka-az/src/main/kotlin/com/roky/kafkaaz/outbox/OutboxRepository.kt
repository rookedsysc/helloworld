package com.roky.kafkaaz.outbox

import java.time.Instant
import java.time.OffsetDateTime
import java.util.UUID
import org.jooq.DSLContext
import org.jooq.Field
import org.jooq.JSONB
import org.jooq.Record
import org.jooq.Table
import org.jooq.impl.DSL.currentOffsetDateTime
import org.jooq.impl.DSL.field
import org.jooq.impl.DSL.name
import org.jooq.impl.DSL.table
import org.jooq.impl.SQLDataType
import org.springframework.stereotype.Repository
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

@Repository
class OutboxRepository(
    private val dslContext: DSLContext
) {

    fun create(transactionDslContext: DSLContext, event: OutboxEvent): Mono<OutboxEvent> {
        return Mono.from(
            transactionDslContext.insertInto(OUTBOX_EVENTS)
                .set(ID, event.id)
                .set(AGGREGATE_ID, event.aggregateId)
                .set(EVENT_TYPE, event.eventType)
                .set(PAYLOAD, event.payload)
                .returning(*OUTBOX_EVENT_FIELDS)
        ).map(::toOutboxEvent)
    }

    fun findPending(limit: Int): Flux<OutboxEvent> {
        return Flux.from(
            dslContext.select(*OUTBOX_EVENT_FIELDS)
                .from(OUTBOX_EVENTS)
                .where(PUBLISHED_AT.isNull)
                .orderBy(CREATED_AT.asc())
                .limit(limit)
        ).map(::toOutboxEvent)
    }

    fun markPublished(id: UUID): Mono<Void> {
        return Mono.from(
            dslContext.update(OUTBOX_EVENTS)
                .set(PUBLISHED_AT, currentOffsetDateTime())
                .where(ID.eq(id))
        ).then()
    }

    private fun toOutboxEvent(record: Record): OutboxEvent {
        return OutboxEvent(
            id = record[ID]!!,
            aggregateId = record[AGGREGATE_ID]!!,
            eventType = record[EVENT_TYPE]!!,
            payload = record[PAYLOAD]!!,
            createdAt = record[CREATED_AT]?.toInstant(),
            publishedAt = record[PUBLISHED_AT]?.toInstant()
        )
    }

    private companion object {
        private val OUTBOX_EVENTS: Table<Record> = table(name("outbox_events"))
        private val ID: Field<UUID> = field(name("id"), UUID::class.java)
        private val AGGREGATE_ID: Field<Long> = field(name("aggregate_id"), Long::class.java)
        private val EVENT_TYPE: Field<OutboxEventType> = field(
            name("event_type"),
            SQLDataType.VARCHAR.asEnumDataType(OutboxEventType::class.java)
        )
        private val PAYLOAD: Field<JSONB> = field(name("payload"), JSONB::class.java)
        private val CREATED_AT: Field<OffsetDateTime> = field(name("created_at"), OffsetDateTime::class.java)
        private val PUBLISHED_AT: Field<OffsetDateTime> = field(name("published_at"), OffsetDateTime::class.java)
        private val OUTBOX_EVENT_FIELDS = arrayOf(ID, AGGREGATE_ID, EVENT_TYPE, PAYLOAD, CREATED_AT, PUBLISHED_AT)
    }
}
