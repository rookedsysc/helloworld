package com.roky.kafkaaz.member.domain

import com.roky.kafkaaz.common.domain.BaseEntity
import java.time.Instant

data class Member(
    val id: Long? = null,
    val loginId: String,
    val password: String,
    override val createdAt: Instant? = null,
    override val updatedAt: Instant? = null
) : BaseEntity(createdAt, updatedAt)
