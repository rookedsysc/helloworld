package com.roky.kafkaaz.auth.controller

import com.roky.kafkaaz.support.PostgreSQLTestContainerSupport
import java.time.Instant
import kotlin.test.assertFalse
import kotlin.test.assertNotEquals
import kotlin.test.assertNotNull
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
import org.springframework.security.oauth2.jose.jws.MacAlgorithm
import org.springframework.security.oauth2.jwt.JwtClaimsSet
import org.springframework.security.oauth2.jwt.JwtEncoder
import org.springframework.security.oauth2.jwt.JwtEncoderParameters
import org.springframework.security.oauth2.jwt.JwsHeader
import org.springframework.test.web.reactive.server.WebTestClient
import reactor.core.publisher.Mono

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWebTestClient
class AuthControllerIntegrationTests @Autowired constructor(
    private val webTestClient: WebTestClient,
    private val dslContext: DSLContext,
    private val jwtEncoder: JwtEncoder
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
    fun `new login id creates a member and returns a jwt`() {
        val response = login("roky", "password123")
            .expectStatus().isOk
            .expectBody(Map::class.java)
            .returnResult()
            .responseBody

        assertNotNull(response)
        assertNotNull(response["accessToken"])
        assertFalse((response["accessToken"] as String).isBlank())
        kotlin.test.assertEquals("Bearer", response["tokenType"])
        kotlin.test.assertEquals(true, response["isNewMember"])

        val member = Mono.from(
            dslContext.select(LOGIN_ID, PASSWORD)
                .from(MEMBERS)
                .where(LOGIN_ID.eq("roky"))
        ).block()

        assertNotNull(member)
        assertNotEquals("password123", member[PASSWORD])
    }

    @Test
    fun `existing login id authenticates without creating another member`() {
        login("roky", "password123").expectStatus().isOk

        login("roky", "password123")
            .expectStatus().isOk
            .expectBody()
            .jsonPath("$.isNewMember").isEqualTo(false)

        webTestClient.post()
            .uri("/api/v1/auth/login")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(mapOf("id" to "roky", "password" to "wrong-password"))
            .exchange()
            .expectStatus().isUnauthorized

        val memberCount = Mono.from(
            dslContext.selectCount().from(MEMBERS)
        ).map { it.value1() }.block()

        kotlin.test.assertEquals(1, memberCount)
    }

    @Test
    fun `invalid login input is rejected`() {
        login(" ", "password123").expectStatus().isBadRequest
        login("a".repeat(51), "password123").expectStatus().isBadRequest
        login("roky", "1234567").expectStatus().isBadRequest
    }

    @Test
    fun `protected post creation rejects a missing token`() {
        webTestClient.post()
            .uri("/api/v1/posts")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(mapOf("title" to "title", "content" to "content"))
            .exchange()
            .expectStatus().isUnauthorized
            .expectBody()
            .jsonPath("$.status").isEqualTo(401)
    }

    @Test
    fun `protected api rejects tampered and expired tokens`() {
        val accessToken = login("roky", "password123")
            .expectStatus().isOk
            .expectBody(Map::class.java)
            .returnResult()
            .responseBody
            ?.get("accessToken") as String
        val tamperedToken = accessToken.dropLast(1) + if (accessToken.last() == 'a') "b" else "a"

        createPostWithToken(tamperedToken).expectStatus().isUnauthorized
        createPostWithToken(expiredToken()).expectStatus().isUnauthorized
    }

    private fun login(id: String, password: String): WebTestClient.ResponseSpec {
        return webTestClient.post()
            .uri("/api/v1/auth/login")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(mapOf("id" to id, "password" to password))
            .exchange()
    }

    private fun createPostWithToken(accessToken: String): WebTestClient.ResponseSpec {
        return webTestClient.post()
            .uri("/api/v1/posts")
            .headers { it.setBearerAuth(accessToken) }
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(mapOf("title" to "title", "content" to "content"))
            .exchange()
    }

    private fun expiredToken(): String {
        val now = Instant.now()
        val claims = JwtClaimsSet.builder()
            .issuer("kafka-az-test")
            .issuedAt(now.minusSeconds(7200))
            .expiresAt(now.minusSeconds(3600))
            .subject("1")
            .build()
        val header = JwsHeader.with(MacAlgorithm.HS256).build()
        return jwtEncoder.encode(JwtEncoderParameters.from(header, claims)).tokenValue
    }

    private companion object {
        private val MEMBERS: Table<Record> = table(name("members"))
        private val POSTS: Table<Record> = table(name("posts"))
        private val COMMENTS: Table<Record> = table(name("comments"))
        private val NOTIFICATIONS: Table<Record> = table(name("notifications"))
        private val LOGIN_ID: Field<String> = field(name("login_id"), String::class.java)
        private val PASSWORD: Field<String> = field(name("password"), String::class.java)
    }
}
