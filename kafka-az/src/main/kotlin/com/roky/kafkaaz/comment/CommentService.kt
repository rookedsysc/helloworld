package com.roky.kafkaaz.comment

import com.roky.kafkaaz.post.PostRepository
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.web.server.ResponseStatusException
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

@Service
class CommentService(
    private val postRepository: PostRepository,
    private val commentRepository: CommentRepository
) {

    fun create(memberId: Long, postId: Long, request: CommentCreateRequest): Mono<CommentResponse> {
        return requirePost(postId)
            .then(
                commentRepository.create(
                    Comment(
                        postId = postId,
                        memberId = memberId,
                        content = request.content
                    )
                )
            )
            .map(CommentResponse::from)
    }

    fun findAll(postId: Long): Flux<CommentResponse> {
        return requirePost(postId)
            .thenMany(commentRepository.findAllByPostIdOrderByCreatedAtAscIdAsc(postId))
            .map(CommentResponse::from)
    }

    private fun requirePost(postId: Long): Mono<Void> {
        return postRepository.findById(postId)
            .switchIfEmpty(Mono.error(postNotFound(postId)))
            .then()
    }

    private fun postNotFound(postId: Long): ResponseStatusException {
        return ResponseStatusException(HttpStatus.NOT_FOUND, "Post not found: $postId")
    }
}
