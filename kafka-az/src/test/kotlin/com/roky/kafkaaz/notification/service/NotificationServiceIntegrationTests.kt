package com.roky.kafkaaz.notification.service

import com.roky.kafkaaz.comment.domain.Comment
import com.roky.kafkaaz.comment.repository.CommentRepository
import com.roky.kafkaaz.member.domain.Member
import com.roky.kafkaaz.member.repository.MemberRepository
import com.roky.kafkaaz.post.domain.Post
import com.roky.kafkaaz.post.repository.PostRepository
import com.roky.kafkaaz.support.PostgreSQLTestContainerSupport
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import org.jooq.DSLContext
import org.jooq.Field
import org.jooq.Record
import org.jooq.Table
import org.jooq.impl.DSL.field
import org.jooq.impl.DSL.name
import org.jooq.impl.DSL.table
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import reactor.core.publisher.Mono

@SpringBootTest
class NotificationServiceIntegrationTests @Autowired constructor(
    private val notificationService: NotificationService,
    private val memberRepository: MemberRepository,
    private val postRepository: PostRepository,
    private val commentRepository: CommentRepository,
    private val dslContext: DSLContext
) : PostgreSQLTestContainerSupport() {

    @BeforeEach
    fun setUp() {
        Mono.from(dslContext.deleteFrom(NOTIFICATIONS))
            .then(Mono.from(dslContext.deleteFrom(COMMENTS)))
            .then(Mono.from(dslContext.deleteFrom(POSTS)))
            .then(Mono.from(dslContext.deleteFrom(MEMBERS)))
            .block()
    }

    @Test
    fun `creates a notification for another member comment`() {
        val fixture = createFixture()

        val notification = notificationService.create(
            recipientMemberId = fixture.ownerId,
            actorMemberId = fixture.commenterId,
            postId = fixture.postId,
            commentId = fixture.commentId,
            content = "Comment content"
        ).block()

        assertNotNull(notification)
        assertEquals(fixture.ownerId, notification.recipientMemberId)
        assertEquals(fixture.commentId, notification.commentId)
        assertEquals(1, notificationCount())
    }

    @Test
    fun `duplicate comment notification remains one row`() {
        val fixture = createFixture()

        repeat(2) {
            notificationService.create(
                fixture.ownerId,
                fixture.commenterId,
                fixture.postId,
                fixture.commentId,
                "Comment content"
            ).block()
        }

        assertEquals(1, notificationCount())
    }

    @Test
    fun `self comment does not create a notification`() {
        val fixture = createFixture(commenterIsOwner = true)

        val notification = notificationService.create(
            fixture.ownerId,
            fixture.ownerId,
            fixture.postId,
            fixture.commentId,
            "Self comment"
        ).block()

        assertNull(notification)
        assertEquals(0, notificationCount())
    }

    private fun createFixture(commenterIsOwner: Boolean = false): Fixture {
        val owner = memberRepository.create(Member(loginId = "owner", password = "encoded")).block()!!
        val commenter = if (commenterIsOwner) {
            owner
        } else {
            memberRepository.create(Member(loginId = "commenter", password = "encoded")).block()!!
        }
        val post = postRepository.create(
            Post(memberId = owner.id!!, title = "Post", content = "Content")
        ).block()!!
        val comment = commentRepository.create(
            Comment(postId = post.id!!, memberId = commenter.id!!, content = "Comment content")
        ).block()!!

        return Fixture(owner.id, commenter.id, post.id, comment.id!!)
    }

    private fun notificationCount(): Int {
        return Mono.from(dslContext.selectCount().from(NOTIFICATIONS))
            .map { it.value1() }
            .block()!!
    }

    private data class Fixture(
        val ownerId: Long,
        val commenterId: Long,
        val postId: Long,
        val commentId: Long
    )

    private companion object {
        private val MEMBERS: Table<Record> = table(name("members"))
        private val POSTS: Table<Record> = table(name("posts"))
        private val COMMENTS: Table<Record> = table(name("comments"))
        private val NOTIFICATIONS: Table<Record> = table(name("notifications"))
    }
}
