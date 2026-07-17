package com.roky.kafkaaz.common

import io.swagger.v3.oas.annotations.media.Schema

@Schema(description = "API 에러 응답")
data class ApiErrorResponse(
    @field:Schema(description = "HTTP 상태 코드", example = "400")
    val status: Int,

    @field:Schema(description = "에러 메시지", example = "Invalid request")
    val message: String
)
