package com.roky.kafkaaz.comment.controller

import com.roky.kafkaaz.support.PostgreSQLTestContainerSupport
import kotlin.test.assertNotNull
import org.jooq.DSLContext
import org.jooq.Record
import org.jooq.Table
import org.jooq.impl.DSL.name
import org.jooq.impl.DSL.table
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.webtestclient.autoconfigure.AutoConfigureWebTestClient
import org.springframework.http.MediaType
import org.springframework.test.web.reactive.server.WebTestClient
import reactor.core.publisher.Mono

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWebTestClient
class CommentControllerIntegrationTests @Autowired constructor(
    private val webTestClient: WebTestClient,
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
    fun `authenticated member creates a comment`() {
        val ownerToken = login("owner")
        val commenter = loginWithMemberId("commenter")
        val postId = createPost(ownerToken, "Post", "Content")

        webTestClient.post()
            .uri("/api/v1/posts/{postId}/comments", postId)
            .headers { it.setBearerAuth(commenter.accessToken) }
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(mapOf("content" to "First comment"))
            .exchange()
            .expectStatus().isCreated
            .expectBody()
            .jsonPath("$.postId").isEqualTo(postId)
            .jsonPath("$.memberId").isEqualTo(commenter.memberId)
            .jsonPath("$.content").isEqualTo("First comment")
            .jsonPath("$.createdAt").isNotEmpty
    }

    @Test
    fun `comment list is public ordered oldest first and isolated by post`() {
        val ownerToken = login("owner")
        val commenterToken = login("commenter")
        val postId = createPost(ownerToken, "Post", "Content")
        val otherPostId = createPost(ownerToken, "Other", "Other content")

        createComment(commenterToken, postId, "First")
        createComment(commenterToken, postId, "Second")
        createComment(commenterToken, otherPostId, "Other post comment")

        webTestClient.get()
            .uri("/api/v1/posts/{postId}/comments", postId)
            .exchange()
            .expectStatus().isOk
            .expectBody()
            .jsonPath("$[0].content").isEqualTo("First")
            .jsonPath("$[1].content").isEqualTo("Second")
            .jsonPath("$[2]").doesNotExist()
    }

    @Test
    fun `missing post and invalid content are rejected`() {
        val token = login("commenter")

        webTestClient.post()
            .uri("/api/v1/posts/{postId}/comments", 999999)
            .headers { it.setBearerAuth(token) }
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(mapOf("content" to "comment"))
            .exchange()
            .expectStatus().isNotFound

        webTestClient.get()
            .uri("/api/v1/posts/{postId}/comments", 999999)
            .exchange()
            .expectStatus().isNotFound

        val postId = createPost(token, "Post", "Content")
        createCommentRequest(token, postId, " ").expectStatus().isBadRequest
        createCommentRequest(token, postId, "a".repeat(1001)).expectStatus().isBadRequest
    }

    private fun login(id: String): String {
        return loginResponse(id)["accessToken"] as String
    }

    private fun loginWithMemberId(id: String): LoginFixture {
        val response = loginResponse(id)
        val memberId = Mono.from(
            dslContext.select(org.jooq.impl.DSL.field(name("id"), Long::class.java))
                .from(MEMBERS)
                .where(org.jooq.impl.DSL.field(name("login_id"), String::class.java).eq(id))
        ).map { it.value1() }.block()

        return LoginFixture(response["accessToken"] as String, assertNotNull(memberId))
    }

    private fun loginResponse(id: String): Map<*, *> {
        val response = webTestClient.post()
            .uri("/api/v1/auth/login")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(mapOf("id" to id, "password" to "password123"))
            .exchange()
            .expectStatus().isOk
            .expectBody(Map::class.java)
            .returnResult()
            .responseBody

        return assertNotNull(response)
    }

    private fun createPost(accessToken: String, title: String, content: String): Long {
        val response = webTestClient.post()
            .uri("/api/v1/posts")
            .headers { it.setBearerAuth(accessToken) }
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(mapOf("title" to title, "content" to content))
            .exchange()
            .expectStatus().isCreated
            .expectBody(Map::class.java)
            .returnResult()
            .responseBody

        return (assertNotNull(response)["id"] as Number).toLong()
    }

    private fun createComment(accessToken: String, postId: Long, content: String) {
        createCommentRequest(accessToken, postId, content).expectStatus().isCreated
    }

    private fun createCommentRequest(
        accessToken: String,
        postId: Long,
        content: String
    ): WebTestClient.ResponseSpec {
        return webTestClient.post()
            .uri("/api/v1/posts/{postId}/comments", postId)
            .headers { it.setBearerAuth(accessToken) }
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(mapOf("content" to content))
            .exchange()
    }

    private data class LoginFixture(
        val accessToken: String,
        val memberId: Long
    )

    private companion object {
        private val MEMBERS: Table<Record> = table(name("members"))
        private val POSTS: Table<Record> = table(name("posts"))
        private val COMMENTS: Table<Record> = table(name("comments"))
        private val NOTIFICATIONS: Table<Record> = table(name("notifications"))
    }
}
