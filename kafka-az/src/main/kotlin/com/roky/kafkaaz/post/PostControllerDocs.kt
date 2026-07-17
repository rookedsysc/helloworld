package com.roky.kafkaaz.post

import com.roky.kafkaaz.common.ApiErrorResponse
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.security.oauth2.jwt.Jwt
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

@Tag(name = "게시물", description = "게시물 CRUD API")
interface PostControllerDocs {

    @Operation(summary = "게시물 생성", description = "인증 회원을 작성자로 하여 게시물을 생성합니다.")
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponses(value = [ApiResponse(responseCode = "201", description = "생성 성공"), ApiResponse(responseCode = "401", description = "인증 실패", content = [Content(schema = Schema(implementation = ApiErrorResponse::class))])])
    fun create(@Parameter(hidden = true) jwt: Jwt, request: PostCreateRequest): Mono<PostResponse>

    @Operation(summary = "게시물 목록 조회", description = "게시물을 최신 ID 순서로 조회합니다.")
    fun findAll(): Flux<PostResponse>

    @Operation(summary = "게시물 단건 조회", description = "게시물 ID로 게시물을 조회합니다.")
    @ApiResponses(value = [ApiResponse(responseCode = "200", description = "조회 성공"), ApiResponse(responseCode = "404", description = "게시물 없음", content = [Content(schema = Schema(implementation = ApiErrorResponse::class))])])
    fun findById(id: Long): Mono<PostResponse>

    @Operation(summary = "게시물 수정", description = "작성자만 게시물을 수정할 수 있습니다.")
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponses(value = [ApiResponse(responseCode = "200", description = "수정 성공"), ApiResponse(responseCode = "401", description = "인증 실패", content = [Content(schema = Schema(implementation = ApiErrorResponse::class))]), ApiResponse(responseCode = "403", description = "작성자 아님", content = [Content(schema = Schema(implementation = ApiErrorResponse::class))]), ApiResponse(responseCode = "404", description = "게시물 없음", content = [Content(schema = Schema(implementation = ApiErrorResponse::class))])])
    fun update(@Parameter(hidden = true) jwt: Jwt, id: Long, request: PostUpdateRequest): Mono<PostResponse>

    @Operation(summary = "게시물 삭제", description = "작성자만 게시물을 삭제할 수 있습니다.")
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponses(value = [ApiResponse(responseCode = "204", description = "삭제 성공"), ApiResponse(responseCode = "401", description = "인증 실패", content = [Content(schema = Schema(implementation = ApiErrorResponse::class))]), ApiResponse(responseCode = "403", description = "작성자 아님", content = [Content(schema = Schema(implementation = ApiErrorResponse::class))]), ApiResponse(responseCode = "404", description = "게시물 없음", content = [Content(schema = Schema(implementation = ApiErrorResponse::class))])])
    fun delete(@Parameter(hidden = true) jwt: Jwt, id: Long): Mono<Void>
}
