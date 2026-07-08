package com.roky.kafkaaz.post

import io.swagger.v3.oas.annotations.media.Schema

data class PostResponse(
    @Schema(description = "게시물 ID", example = "1")
    val id: Long,

    @Schema(description = "게시물 제목", example = "First post")
    val title: String,

    @Schema(description = "게시물 본문", example = "Hello Kafka AZ")
    val content: String
) {
    companion object {
        fun from(post: Post): PostResponse {
            return PostResponse(
                id = post.id ?: throw IllegalStateException("Post id must not be null"),
                title = post.title,
                content = post.content
            )
        }
    }
}
