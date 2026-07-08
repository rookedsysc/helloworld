package com.roky.kafkaaz.post

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
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
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

@RestController
@RequestMapping("/api/v1/posts")
@Tag(name = "Post API", description = "게시물 CRUD API")
class PostController(
    private val postService: PostService
) {

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "게시물 생성")
    fun create(@Valid @RequestBody request: PostCreateRequest): Mono<PostResponse> {
        return postService.create(request)
    }

    @GetMapping
    @Operation(summary = "게시물 목록 조회")
    fun findAll(): Flux<PostResponse> {
        return postService.findAll()
    }

    @GetMapping("/{id}")
    @Operation(summary = "게시물 단건 조회")
    fun findById(@PathVariable id: Long): Mono<PostResponse> {
        return postService.findById(id)
    }

    @PutMapping("/{id}")
    @Operation(summary = "게시물 수정")
    fun update(
        @PathVariable id: Long,
        @Valid @RequestBody request: PostUpdateRequest
    ): Mono<PostResponse> {
        return postService.update(id, request)
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "게시물 삭제")
    fun delete(@PathVariable id: Long): Mono<Void> {
        return postService.delete(id)
    }
}
