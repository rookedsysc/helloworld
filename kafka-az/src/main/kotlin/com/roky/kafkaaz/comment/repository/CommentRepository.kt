package com.roky.kafkaaz.comment.repository

import com.roky.kafkaaz.comment.domain.Comment
import java.time.OffsetDateTime
import org.jooq.DSLContext
import org.jooq.Field
import org.jooq.Record
import org.jooq.Table
import org.jooq.impl.DSL.field
import org.jooq.impl.DSL.name
import org.jooq.impl.DSL.table
import org.springframework.stereotype.Repository
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

@Repository
class CommentRepository(
    private val dslContext: DSLContext
) {

    fun create(comment: Comment): Mono<Comment> {
        return Mono.from(
            dslContext.insertInto(COMMENTS)
                .set(POST_ID, comment.postId)
                .set(MEMBER_ID, comment.memberId)
                .set(CONTENT, comment.content)
                .returning(*COMMENT_FIELDS)
        ).map(::toComment)
    }

    fun findAllByPostIdOrderByCreatedAtAscIdAsc(postId: Long): Flux<Comment> {
        return Flux.from(
            dslContext.select(*COMMENT_FIELDS)
                .from(COMMENTS)
                .where(POST_ID.eq(postId))
                .orderBy(CREATED_AT.asc(), ID.asc())
        ).map(::toComment)
    }

    private fun toComment(record: Record): Comment {
        return Comment(
            id = record[ID],
            postId = record[POST_ID]!!,
            memberId = record[MEMBER_ID]!!,
            content = record[CONTENT]!!,
            createdAt = record[CREATED_AT]?.toInstant(),
            updatedAt = record[UPDATED_AT]?.toInstant()
        )
    }

    private companion object {
        private val COMMENTS: Table<Record> = table(name("comments"))
        private val ID: Field<Long> = field(name("id"), Long::class.java)
        private val POST_ID: Field<Long> = field(name("post_id"), Long::class.java)
        private val MEMBER_ID: Field<Long> = field(name("member_id"), Long::class.java)
        private val CONTENT: Field<String> = field(name("content"), String::class.java)
        private val CREATED_AT: Field<OffsetDateTime> = field(name("created_at"), OffsetDateTime::class.java)
        private val UPDATED_AT: Field<OffsetDateTime> = field(name("updated_at"), OffsetDateTime::class.java)
        private val COMMENT_FIELDS = arrayOf(ID, POST_ID, MEMBER_ID, CONTENT, CREATED_AT, UPDATED_AT)
    }
}
