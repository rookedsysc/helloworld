package com.roky.kafkaaz.auth

import com.roky.kafkaaz.common.ApiErrorResponse
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag

@Tag(name = "인증", description = "로그인 겸 자동 회원가입 API")
interface AuthControllerDocs {

    @Operation(
        summary = "로그인 겸 자동 회원가입",
        description = "ID가 없으면 회원을 생성하고, 있으면 비밀번호를 검증한 뒤 JWT를 발급합니다."
    )
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "로그인 또는 자동 회원가입 성공"),
            ApiResponse(responseCode = "400", description = "입력값 검증 실패", content = [Content(schema = Schema(implementation = ApiErrorResponse::class))]),
            ApiResponse(responseCode = "401", description = "비밀번호 불일치", content = [Content(schema = Schema(implementation = ApiErrorResponse::class))])
        ]
    )
    fun login(request: LoginRequest): reactor.core.publisher.Mono<LoginResponse>
}
