package com.roky.kafkaaz.post.repository

import com.roky.kafkaaz.post.domain.Post
import java.time.Instant
import java.time.OffsetDateTime
import org.jooq.DSLContext
import org.jooq.Field
import org.jooq.Record
import org.jooq.Table
import org.jooq.impl.DSL.currentOffsetDateTime
import org.jooq.impl.DSL.field
import org.jooq.impl.DSL.name
import org.jooq.impl.DSL.table
import org.springframework.stereotype.Repository
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

@Repository
class PostRepository(
    private val dslContext: DSLContext
) {

    fun create(post: Post): Mono<Post> {
        return create(dslContext, post)
    }

    fun create(transactionDslContext: DSLContext, post: Post): Mono<Post> {
        return Mono.from(
            transactionDslContext.insertInto(POSTS)
                .set(MEMBER_ID, post.memberId)
                .set(TITLE, post.title)
                .set(CONTENT, post.content)
                .returning(*POST_FIELDS)
        ).map(::toPost)
    }

    fun findAllByOrderByIdDesc(): Flux<Post> {
        return Flux.from(
            dslContext.select(*POST_FIELDS)
                .from(POSTS)
                .orderBy(ID.desc())
        ).map(::toPost)
    }

    fun findById(id: Long): Mono<Post> {
        return Mono.from(
            dslContext.select(*POST_FIELDS)
                .from(POSTS)
                .where(ID.eq(id))
        ).map(::toPost)
    }

    fun update(id: Long, title: String, content: String): Mono<Post> {
        return Mono.from(
            dslContext.update(POSTS)
                .set(TITLE, title)
                .set(CONTENT, content)
                .set(UPDATED_AT, currentOffsetDateTime())
                .where(ID.eq(id))
                .returning(*POST_FIELDS)
        ).map(::toPost)
    }

    fun deleteById(id: Long): Mono<Long> {
        return Mono.from(
            dslContext.deleteFrom(POSTS)
                .where(ID.eq(id))
                .returning(ID)
        ).map { it[ID]!! }
    }

    fun deleteAll(): Mono<Void> {
        return Mono.from(dslContext.deleteFrom(POSTS)).then()
    }

    private fun toPost(record: Record): Post {
        return Post(
            id = record[ID],
            memberId = record[MEMBER_ID]!!,
            title = record[TITLE]!!,
            content = record[CONTENT]!!,
            createdAt = record[CREATED_AT]?.toInstant(),
            updatedAt = record[UPDATED_AT]?.toInstant()
        )
    }

    private companion object {
        private val POSTS: Table<Record> = table(name("posts"))
        private val ID: Field<Long> = field(name("id"), Long::class.java)
        private val MEMBER_ID: Field<Long> = field(name("member_id"), Long::class.java)
        private val TITLE: Field<String> = field(name("title"), String::class.java)
        private val CONTENT: Field<String> = field(name("content"), String::class.java)
        private val CREATED_AT: Field<OffsetDateTime> = field(name("created_at"), OffsetDateTime::class.java)
        private val UPDATED_AT: Field<OffsetDateTime> = field(name("updated_at"), OffsetDateTime::class.java)
        private val POST_FIELDS = arrayOf(ID, MEMBER_ID, TITLE, CONTENT, CREATED_AT, UPDATED_AT)
    }
}
