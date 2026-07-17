package com.roky.kafkaaz.comment

import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.server.ResponseStatusException
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

@RestController
@RequestMapping("/api/v1/posts/{postId}/comments")
class CommentController(
    private val commentService: CommentService
) : CommentControllerDocs {

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    override fun create(
        @AuthenticationPrincipal jwt: Jwt,
        @PathVariable postId: Long,
        @Valid @RequestBody request: CommentCreateRequest
    ): Mono<CommentResponse> {
        return commentService.create(memberId(jwt), postId, request)
    }

    @GetMapping
    override fun findAll(@PathVariable postId: Long): Flux<CommentResponse> {
        return commentService.findAll(postId)
    }

    private fun memberId(jwt: Jwt): Long {
        return jwt.subject?.toLongOrNull()
            ?: throw ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid JWT subject")
    }
}
