package com.roky.kafkaaz.post.controller

import com.roky.kafkaaz.post.dto.PostCreateRequest
import com.roky.kafkaaz.post.dto.PostResponse
import com.roky.kafkaaz.post.dto.PostUpdateRequest
import com.roky.kafkaaz.post.service.PostService
import jakarta.validation.Valid
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.server.ResponseStatusException
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

@RestController
@RequestMapping("/api/v1/posts")
class PostController(
    private val postService: PostService
) : PostControllerDocs {

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    override fun create(
        @AuthenticationPrincipal jwt: Jwt,
        @Valid @RequestBody request: PostCreateRequest
    ): Mono<PostResponse> {
        return postService.create(memberId(jwt), request)
    }

    @GetMapping
    override fun findAll(): Flux<PostResponse> {
        return postService.findAll()
    }

    @GetMapping("/{id}")
    override fun findById(@PathVariable id: Long): Mono<PostResponse> {
        return postService.findById(id)
    }

    @PutMapping("/{id}")
    override fun update(
        @AuthenticationPrincipal jwt: Jwt,
        @PathVariable id: Long,
        @Valid @RequestBody request: PostUpdateRequest
    ): Mono<PostResponse> {
        return postService.update(memberId(jwt), id, request)
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    override fun delete(
        @AuthenticationPrincipal jwt: Jwt,
        @PathVariable id: Long
    ): Mono<Void> {
        return postService.delete(memberId(jwt), id)
    }

    private fun memberId(jwt: Jwt): Long {
        return jwt.subject?.toLongOrNull()
            ?: throw ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid JWT subject")
    }
}
