package com.roky.kafkaaz.post

import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.web.server.ResponseStatusException
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

@Service
class PostService(
    private val postRepository: PostRepository
) {

    fun create(memberId: Long, request: PostCreateRequest): Mono<PostResponse> {
        val post = Post(
            memberId = memberId,
            title = request.title,
            content = request.content
        )

        return postRepository.create(post)
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
