package com.roky.kafkaaz.member

import com.roky.kafkaaz.common.BaseEntity
import java.time.Instant

data class Member(
    val id: Long? = null,
    val loginId: String,
    val password: String,
    override val createdAt: Instant? = null,
    override val updatedAt: Instant? = null
) : BaseEntity(createdAt, updatedAt)
