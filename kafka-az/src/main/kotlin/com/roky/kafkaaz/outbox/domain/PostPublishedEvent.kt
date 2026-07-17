package com.roky.kafkaaz.outbox.domain

import com.roky.kafkaaz.post.domain.Post
import java.time.Instant
import java.util.UUID

data class PostPublishedEvent(
    val eventId: UUID,
    val postId: Long,
    val title: String,
    val content: String,
    val occurredAt: Instant
) {
    companion object {
        val TYPE = OutboxEventType.POST_PUBLISHED

        fun from(post: Post, eventId: UUID): PostPublishedEvent {
            return PostPublishedEvent(
                eventId = eventId,
                postId = post.id!!,
                title = post.title,
                content = post.content,
                occurredAt = post.createdAt ?: Instant.now()
            )
        }
    }
}
