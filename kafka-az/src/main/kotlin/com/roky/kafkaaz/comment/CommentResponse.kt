package com.roky.kafkaaz.comment

import io.swagger.v3.oas.annotations.media.Schema
import java.time.Instant

@Schema(description = "댓글 응답")
data class CommentResponse(
    @field:Schema(description = "댓글 ID", example = "1")
    val id: Long,

    @field:Schema(description = "게시물 ID", example = "1")
    val postId: Long,

    @field:Schema(description = "댓글 작성자 회원 ID", example = "10")
    val memberId: Long,

    @field:Schema(description = "댓글 내용", example = "좋은 글이네요.")
    val content: String,

    @field:Schema(description = "댓글 작성 시각")
    val createdAt: Instant
) {
    companion object {
        fun from(comment: Comment): CommentResponse {
            return CommentResponse(
                id = requireNotNull(comment.id),
                postId = comment.postId,
                memberId = comment.memberId,
                content = comment.content,
                createdAt = requireNotNull(comment.createdAt)
            )
        }
    }
}
