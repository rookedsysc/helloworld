package com.roky.kafkaaz.auth

import io.swagger.v3.oas.annotations.media.Schema

@Schema(description = "로그인 겸 자동 회원가입 응답")
data class LoginResponse(
    @field:Schema(description = "JWT access token")
    val accessToken: String,

    @field:Schema(description = "인증 scheme", example = "Bearer")
    val tokenType: String = "Bearer",

    @field:Schema(description = "이번 요청에서 신규 회원이 생성됐는지 여부")
    val isNewMember: Boolean
)
