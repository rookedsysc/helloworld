package com.roky.kafkaaz.outbox

import org.jooq.EnumType

enum class OutboxEventType : EnumType {
    POST_PUBLISHED;

    override fun getLiteral(): String = name

    override fun getName(): String = "outbox_event_type"
}
