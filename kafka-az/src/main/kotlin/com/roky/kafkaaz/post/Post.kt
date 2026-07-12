package com.roky.kafkaaz.post

import com.roky.kafkaaz.common.BaseEntity
import java.time.Instant

data class Post(
    val id: Long? = null,
    val title: String,
    val content: String,
    override val createdAt: Instant? = null,
    override val updatedAt: Instant? = null
) : BaseEntity(createdAt, updatedAt)
