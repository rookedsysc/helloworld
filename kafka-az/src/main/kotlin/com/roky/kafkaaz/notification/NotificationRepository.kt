package com.roky.kafkaaz.notification

import java.time.OffsetDateTime
import org.jooq.DSLContext
import org.jooq.Field
import org.jooq.Record
import org.jooq.Table
import org.jooq.impl.DSL.field
import org.jooq.impl.DSL.name
import org.jooq.impl.DSL.table
import org.springframework.stereotype.Repository
import reactor.core.publisher.Mono

@Repository
class NotificationRepository(
    private val dslContext: DSLContext
) {

    fun createIfAbsent(notification: Notification): Mono<Notification> {
        return Mono.from(
            dslContext.insertInto(NOTIFICATIONS)
                .set(RECIPIENT_MEMBER_ID, notification.recipientMemberId)
                .set(ACTOR_MEMBER_ID, notification.actorMemberId)
                .set(POST_ID, notification.postId)
                .set(COMMENT_ID, notification.commentId)
                .set(CONTENT, notification.content)
                .onConflict(RECIPIENT_MEMBER_ID, COMMENT_ID)
                .doNothing()
                .returning(*NOTIFICATION_FIELDS)
        )
            .map(::toNotification)
            .switchIfEmpty(findByRecipientMemberIdAndCommentId(notification.recipientMemberId, notification.commentId))
    }

    private fun findByRecipientMemberIdAndCommentId(recipientMemberId: Long, commentId: Long): Mono<Notification> {
        return Mono.from(
            dslContext.select(*NOTIFICATION_FIELDS)
                .from(NOTIFICATIONS)
                .where(RECIPIENT_MEMBER_ID.eq(recipientMemberId))
                .and(COMMENT_ID.eq(commentId))
        ).map(::toNotification)
    }

    private fun toNotification(record: Record): Notification {
        return Notification(
            id = record[ID],
            recipientMemberId = record[RECIPIENT_MEMBER_ID]!!,
            actorMemberId = record[ACTOR_MEMBER_ID]!!,
            postId = record[POST_ID]!!,
            commentId = record[COMMENT_ID]!!,
            content = record[CONTENT]!!,
            createdAt = record[CREATED_AT]?.toInstant()
        )
    }

    private companion object {
        private val NOTIFICATIONS: Table<Record> = table(name("notifications"))
        private val ID: Field<Long> = field(name("id"), Long::class.java)
        private val RECIPIENT_MEMBER_ID: Field<Long> = field(name("recipient_member_id"), Long::class.java)
        private val ACTOR_MEMBER_ID: Field<Long> = field(name("actor_member_id"), Long::class.java)
        private val POST_ID: Field<Long> = field(name("post_id"), Long::class.java)
        private val COMMENT_ID: Field<Long> = field(name("comment_id"), Long::class.java)
        private val CONTENT: Field<String> = field(name("content"), String::class.java)
        private val CREATED_AT: Field<OffsetDateTime> = field(name("created_at"), OffsetDateTime::class.java)
        private val NOTIFICATION_FIELDS = arrayOf(
            ID,
            RECIPIENT_MEMBER_ID,
            ACTOR_MEMBER_ID,
            POST_ID,
            COMMENT_ID,
            CONTENT,
            CREATED_AT
        )
    }
}
