package com.roky.kafkaaz.comment

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

@Tag(name = "댓글", description = "게시물 댓글 API")
interface CommentControllerDocs {

    @Operation(summary = "댓글 작성", description = "인증 회원이 게시물에 댓글을 작성합니다.")
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponses(value = [ApiResponse(responseCode = "201", description = "작성 성공"), ApiResponse(responseCode = "400", description = "입력값 검증 실패", content = [Content(schema = Schema(implementation = ApiErrorResponse::class))]), ApiResponse(responseCode = "401", description = "인증 실패", content = [Content(schema = Schema(implementation = ApiErrorResponse::class))]), ApiResponse(responseCode = "404", description = "게시물 없음", content = [Content(schema = Schema(implementation = ApiErrorResponse::class))])])
    fun create(@Parameter(hidden = true) jwt: Jwt, postId: Long, request: CommentCreateRequest): Mono<CommentResponse>

    @Operation(summary = "댓글 목록 조회", description = "게시물 댓글을 오래된 순서로 조회합니다.")
    @ApiResponses(value = [ApiResponse(responseCode = "200", description = "조회 성공"), ApiResponse(responseCode = "404", description = "게시물 없음", content = [Content(schema = Schema(implementation = ApiErrorResponse::class))])])
    fun findAll(postId: Long): Flux<CommentResponse>
}
