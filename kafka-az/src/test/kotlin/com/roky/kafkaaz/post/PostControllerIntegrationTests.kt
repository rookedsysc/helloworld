package com.roky.kafkaaz.post

import com.roky.kafkaaz.support.PostgreSQLTestContainerSupport
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue
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
import org.springframework.boot.webtestclient.autoconfigure.AutoConfigureWebTestClient
import org.springframework.http.MediaType
import org.springframework.test.web.reactive.server.WebTestClient
import reactor.core.publisher.Mono
import reactor.core.publisher.Flux

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWebTestClient
class PostControllerIntegrationTests @Autowired constructor(
    private val webTestClient: WebTestClient,
    private val postRepository: PostRepository,
    private val dslContext: DSLContext
) : PostgreSQLTestContainerSupport() {

    @BeforeEach
    fun setUp() {
        Mono.from(dslContext.deleteFrom(NOTIFICATIONS))
            .then(Mono.from(dslContext.deleteFrom(COMMENTS)))
            .then(postRepository.deleteAll())
            .then(Mono.from(dslContext.deleteFrom(MEMBERS)))
            .block()
    }

    @Test
    fun `creates reads updates and deletes a post`() {
        val accessToken = login("post-owner")

        val created = webTestClient.post()
            .uri("/api/v1/posts")
            .headers { it.setBearerAuth(accessToken) }
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(
                mapOf(
                    "title" to "First post",
                    "content" to "Hello Kafka AZ"
                )
            )
            .exchange()
            .expectStatus().isCreated
            .expectBody(Map::class.java)
            .returnResult()
            .responseBody

        assertNotNull(created)
        val postId = (created["id"] as Number).toLong()
        val memberId = (created["memberId"] as Number).toLong()
        assertEquals("First post", created["title"])
        assertEquals("Hello Kafka AZ", created["content"])

        val createdPost = postRepository.findById(postId).block()

        assertNotNull(createdPost)
        val createdAt = assertNotNull(createdPost.createdAt)
        val firstUpdatedAt = assertNotNull(createdPost.updatedAt)
        assertTrue(!firstUpdatedAt.isBefore(createdAt))

        Thread.sleep(50)

        webTestClient.get()
            .uri("/api/v1/posts")
            .exchange()
            .expectStatus().isOk
            .expectBody()
            .jsonPath("$[0].id").isEqualTo(postId)
            .jsonPath("$[0].title").isEqualTo("First post")
            .jsonPath("$[0].content").isEqualTo("Hello Kafka AZ")
            .jsonPath("$[0].memberId").isEqualTo(memberId)

        webTestClient.get()
            .uri("/api/v1/posts/{id}", postId)
            .exchange()
            .expectStatus().isOk
            .expectBody()
            .jsonPath("$.id").isEqualTo(postId)
            .jsonPath("$.title").isEqualTo("First post")
            .jsonPath("$.content").isEqualTo("Hello Kafka AZ")
            .jsonPath("$.memberId").isEqualTo(memberId)

        webTestClient.put()
            .uri("/api/v1/posts/{id}", postId)
            .headers { it.setBearerAuth(accessToken) }
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(
                mapOf(
                    "title" to "Updated post",
                    "content" to "Updated content"
                )
            )
            .exchange()
            .expectStatus().isOk
            .expectBody()
            .jsonPath("$.id").isEqualTo(postId)
            .jsonPath("$.title").isEqualTo("Updated post")
            .jsonPath("$.content").isEqualTo("Updated content")

        val updatedPost = postRepository.findById(postId).block()

        assertNotNull(updatedPost)
        assertEquals(createdAt, updatedPost.createdAt)
        assertTrue(
            assertNotNull(updatedPost.updatedAt).isAfter(firstUpdatedAt),
            "updatedAt should be refreshed on post update"
        )

        webTestClient.delete()
            .uri("/api/v1/posts/{id}", postId)
            .headers { it.setBearerAuth(accessToken) }
            .exchange()
            .expectStatus().isNoContent

        webTestClient.get()
            .uri("/api/v1/posts/{id}", postId)
            .exchange()
            .expectStatus().isNotFound
    }

    @Test
    fun `only the post owner can update or delete`() {
        val ownerToken = login("owner")
        val otherToken = login("other")
        val postId = createPost(ownerToken, "Owner post", "Owner content")

        webTestClient.put()
            .uri("/api/v1/posts/{id}", postId)
            .headers { it.setBearerAuth(otherToken) }
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(mapOf("title" to "Changed", "content" to "Changed"))
            .exchange()
            .expectStatus().isForbidden

        webTestClient.delete()
            .uri("/api/v1/posts/{id}", postId)
            .headers { it.setBearerAuth(otherToken) }
            .exchange()
            .expectStatus().isForbidden

        webTestClient.get()
            .uri("/api/v1/posts/{id}", postId)
            .exchange()
            .expectStatus().isOk
            .expectBody()
            .jsonPath("$.title").isEqualTo("Owner post")
    }

    @Test
    fun `exposes swagger documentation for post api`() {
        webTestClient.get()
            .uri("/v3/api-docs")
            .exchange()
            .expectStatus().isOk
            .expectBody()
            .jsonPath("$.paths['/api/v1/posts']").exists()
            .jsonPath("$.paths['/api/v1/posts/{id}']").exists()
            .jsonPath("$.paths['/api/v1/auth/login']").exists()
            .jsonPath("$.paths['/api/v1/posts/{postId}/comments']").exists()
            .jsonPath("$.components.securitySchemes.bearerAuth").exists()
            .jsonPath("$.components.securitySchemes.bearerAuth.scheme").isEqualTo("bearer")

        val swaggerUiStatus = webTestClient.get()
            .uri("/swagger-ui.html")
            .exchange()
            .returnResult(Void::class.java)
            .status

        assertTrue(
            swaggerUiStatus.is2xxSuccessful || swaggerUiStatus.is3xxRedirection,
            "Swagger UI should be reachable but was $swaggerUiStatus"
        )
    }

    @Test
    fun `rolls back jooq writes in a reactive transaction`() {
        login("transaction-owner")
        val memberId = Mono.from(dslContext.select(MEMBER_ID).from(MEMBERS)).map { it[MEMBER_ID]!! }.block()!!

        Flux.from(
            dslContext.transactionPublisher<Void> { configuration ->
                Flux.from(
                    configuration.dsl().insertInto(POSTS)
                        .set(POST_MEMBER_ID, memberId)
                        .set(TITLE, "First")
                        .set(CONTENT, "First content")
                        .returning(ID)
                )
                    .thenMany(
                        Flux.from(
                            configuration.dsl().insertInto(POSTS)
                                .set(POST_MEMBER_ID, memberId)
                                .set(TITLE, "Second")
                                .set(CONTENT, "Second content")
                                .returning(ID)
                        )
                    )
                    .then(Mono.error<Void>(IllegalStateException("rollback")))
            }
        )
            .onErrorResume(IllegalStateException::class.java) { Mono.empty() }
            .blockLast()

        assertTrue(postRepository.findAllByOrderByIdDesc().collectList().block()!!.isEmpty())
    }

    private fun login(id: String): String {
        val response = webTestClient.post()
            .uri("/api/v1/auth/login")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(mapOf("id" to id, "password" to "password123"))
            .exchange()
            .expectStatus().isOk
            .expectBody(Map::class.java)
            .returnResult()
            .responseBody

        assertNotNull(response)
        return response["accessToken"] as String
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

        assertNotNull(response)
        return (response["id"] as Number).toLong()
    }

    private companion object {
        private val POSTS: Table<Record> = table(name("posts"))
        private val MEMBERS: Table<Record> = table(name("members"))
        private val COMMENTS: Table<Record> = table(name("comments"))
        private val NOTIFICATIONS: Table<Record> = table(name("notifications"))
        private val ID: Field<Long> = field(name("id"), Long::class.java)
        private val MEMBER_ID: Field<Long> = field(name("id"), Long::class.java)
        private val POST_MEMBER_ID: Field<Long> = field(name("member_id"), Long::class.java)
        private val TITLE: Field<String> = field(name("title"), String::class.java)
        private val CONTENT: Field<String> = field(name("content"), String::class.java)
    }
}
