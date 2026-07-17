package com.roky.kafkaaz.post.service

import com.fasterxml.jackson.databind.ObjectMapper
import com.roky.kafkaaz.outbox.domain.OutboxEvent
import com.roky.kafkaaz.outbox.domain.PostPublishedEvent
import com.roky.kafkaaz.outbox.repository.OutboxRepository
import com.roky.kafkaaz.post.domain.Post
import com.roky.kafkaaz.post.dto.PostCreateRequest
import com.roky.kafkaaz.post.dto.PostResponse
import com.roky.kafkaaz.post.dto.PostUpdateRequest
import com.roky.kafkaaz.post.repository.PostRepository
import java.util.UUID
import org.jooq.DSLContext
import org.jooq.JSONB
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.web.server.ResponseStatusException
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

@Service
class PostService(
    private val postRepository: PostRepository,
    private val outboxRepository: OutboxRepository,
    private val dslContext: DSLContext,
    private val objectMapper: ObjectMapper
) {

    fun create(memberId: Long, request: PostCreateRequest): Mono<PostResponse> {
        val post = Post(
            memberId = memberId,
            title = request.title,
            content = request.content
        )

        return Mono.from(
            dslContext.transactionPublisher<Post> { configuration ->
                postRepository.create(configuration.dsl(), post)
                    .flatMap { savedPost ->
                        val eventId = UUID.randomUUID()
                        val event = PostPublishedEvent.from(savedPost, eventId)

                        outboxRepository.create(
                            configuration.dsl(),
                            OutboxEvent(
                                id = eventId,
                                aggregateId = savedPost.id!!,
                                eventType = PostPublishedEvent.TYPE,
                                payload = JSONB.valueOf(objectMapper.writeValueAsString(event))
                            )
                        ).thenReturn(savedPost)
                    }
            }
        )
            .map(PostResponse::from)
    }

    fun findAll(): Flux<PostResponse> {
        return postRepository.findAllByOrderByIdDesc()
            .map(PostResponse::from)
    }

    fun findById(id: Long): Mono<PostResponse> {
        return getPost(id)
            .map(PostResponse::from)
    }

    fun update(memberId: Long, id: Long, request: PostUpdateRequest): Mono<PostResponse> {
        return getPost(id)
            .flatMap { post ->
                validateOwner(memberId, post)
                postRepository.update(id, request.title, request.content)
            }
            .map(PostResponse::from)
    }

    fun delete(memberId: Long, id: Long): Mono<Void> {
        return getPost(id)
            .flatMap { post ->
                validateOwner(memberId, post)
                postRepository.deleteById(id)
            }
            .then()
    }

    private fun getPost(id: Long): Mono<Post> {
        return postRepository.findById(id)
            .switchIfEmpty(postNotFound(id))
    }

    private fun <T : Any> postNotFound(id: Long): Mono<T> {
        return Mono.error(ResponseStatusException(HttpStatus.NOT_FOUND, "Post not found: $id"))
    }

    private fun validateOwner(memberId: Long, post: Post) {
        if (post.memberId != memberId) {
            throw ResponseStatusException(HttpStatus.FORBIDDEN, "Only the post owner can modify this post")
        }
    }
}
