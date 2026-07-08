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

    fun create(request: PostCreateRequest): Mono<PostResponse> {
        val post = Post(
            title = request.title,
            content = request.content
        )

        return postRepository.save(post)
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

    fun update(id: Long, request: PostUpdateRequest): Mono<PostResponse> {
        return getPost(id)
            .map {
                it.update(
                    title = request.title,
                    content = request.content
                )
            }
            .flatMap(postRepository::save)
            .map(PostResponse::from)
    }

    fun delete(id: Long): Mono<Void> {
        return getPost(id)
            .flatMap(postRepository::delete)
    }

    private fun getPost(id: Long): Mono<Post> {
        return postRepository.findById(id)
            .switchIfEmpty(postNotFound(id))
    }

    private fun postNotFound(id: Long): Mono<Post> {
        return Mono.error(ResponseStatusException(HttpStatus.NOT_FOUND, "Post not found: $id"))
    }
}
