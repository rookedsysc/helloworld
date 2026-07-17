package com.roky.kafkaaz.post.dto

import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size

data class PostCreateRequest(
    @field:NotBlank(message = "제목은 비어있을 수 없습니다.")
    @field:Size(max = 100, message = "제목은 100자를 초과할 수 없습니다.")
    @Schema(description = "게시물 제목", example = "First post", maxLength = 100)
    val title: String,

    @field:NotBlank(message = "본문은 비어있을 수 없습니다.")
    @field:Size(max = 5000, message = "본문은 5000자를 초과할 수 없습니다.")
    @Schema(description = "게시물 본문", example = "Hello Kafka AZ", maxLength = 5000)
    val content: String
)

data class PostUpdateRequest(
    @field:NotBlank(message = "제목은 비어있을 수 없습니다.")
    @field:Size(max = 100, message = "제목은 100자를 초과할 수 없습니다.")
    @Schema(description = "게시물 제목", example = "Updated post", maxLength = 100)
    val title: String,

    @field:NotBlank(message = "본문은 비어있을 수 없습니다.")
    @field:Size(max = 5000, message = "본문은 5000자를 초과할 수 없습니다.")
    @Schema(description = "게시물 본문", example = "Updated content", maxLength = 5000)
    val content: String
)
