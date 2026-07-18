package com.roky.kafkaaz.auth.dto

import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size

@Schema(description = "로그인 겸 자동 회원가입 요청")
data class LoginRequest(
    @field:NotBlank(message = "ID는 비어있을 수 없습니다.")
    @field:Size(max = 50, message = "ID는 50자를 초과할 수 없습니다.")
    @field:Schema(description = "로그인 ID", example = "roky", maxLength = 50)
    val id: String,

    @field:Size(min = 8, max = 100, message = "비밀번호는 8자 이상 100자 이하여야 합니다.")
    @field:Schema(description = "비밀번호", example = "password123", minLength = 8, maxLength = 100)
    val password: String
)
