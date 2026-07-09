package com.roky.kafkaaz.post

import com.roky.kafkaaz.support.PostgreSQLTestContainerSupport
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.webtestclient.autoconfigure.AutoConfigureWebTestClient
import org.springframework.http.MediaType
import org.springframework.test.web.reactive.server.WebTestClient

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWebTestClient
class PostControllerIntegrationTests @Autowired constructor(
    private val webTestClient: WebTestClient,
    private val postRepository: PostRepository
) : PostgreSQLTestContainerSupport() {

    @BeforeEach
    fun setUp() {
        postRepository.deleteAll().block()
    }

    @Test
    fun `creates reads updates and deletes a post`() {
        val created = webTestClient.post()
            .uri("/api/v1/posts")
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

        webTestClient.get()
            .uri("/api/v1/posts/{id}", postId)
            .exchange()
            .expectStatus().isOk
            .expectBody()
            .jsonPath("$.id").isEqualTo(postId)
            .jsonPath("$.title").isEqualTo("First post")
            .jsonPath("$.content").isEqualTo("Hello Kafka AZ")

        webTestClient.put()
            .uri("/api/v1/posts/{id}", postId)
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
            .exchange()
            .expectStatus().isNoContent

        webTestClient.get()
            .uri("/api/v1/posts/{id}", postId)
            .exchange()
            .expectStatus().isNotFound
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
}
