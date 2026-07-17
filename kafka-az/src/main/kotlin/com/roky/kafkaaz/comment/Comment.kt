package com.roky.kafkaaz.comment

import com.roky.kafkaaz.common.BaseEntity
import java.time.Instant

data class Comment(
    val id: Long? = null,
    val postId: Long,
    val memberId: Long,
    val content: String,
    override val createdAt: Instant? = null,
    override val updatedAt: Instant? = null
) : BaseEntity(createdAt, updatedAt)
