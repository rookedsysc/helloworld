package com.roky.kafkaaz.common

import java.time.Instant
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.LastModifiedDate
import org.springframework.data.relational.core.mapping.Column

abstract class BaseEntity(
    @CreatedDate
    @Column("created_at")
    open val createdAt: Instant? = null,

    @LastModifiedDate
    @Column("updated_at")
    open val updatedAt: Instant? = null
)
