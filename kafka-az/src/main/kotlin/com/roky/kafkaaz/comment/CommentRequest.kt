package com.roky.kafkaaz.comment

import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size

@Schema(description = "댓글 작성 요청")
data class CommentCreateRequest(
    @field:NotBlank(message = "댓글은 비어있을 수 없습니다.")
    @field:Size(max = 1000, message = "댓글은 1000자를 초과할 수 없습니다.")
    @field:Schema(description = "댓글 내용", example = "좋은 글이네요.", maxLength = 1000)
    val content: String
)
