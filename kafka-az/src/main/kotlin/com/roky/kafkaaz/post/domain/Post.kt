package com.roky.kafkaaz.post.domain

import com.roky.kafkaaz.common.domain.BaseEntity
import java.time.Instant

data class Post(
    val id: Long? = null,
    val memberId: Long,
    val title: String,
    val content: String,
    override val createdAt: Instant? = null,
    override val updatedAt: Instant? = null
) : BaseEntity(createdAt, updatedAt)
