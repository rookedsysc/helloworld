package com.roky.kafkaaz.post

import com.roky.kafkaaz.common.BaseEntity
import java.time.Instant
import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table

@Table(name = "posts")
data class Post(
    @Id
    val id: Long? = null,

    @Column("title")
    val title: String,

    @Column("content")
    val content: String,

    @Column("created_at")
    override val createdAt: Instant? = null,

    @Column("updated_at")
    override val updatedAt: Instant? = null
) : BaseEntity(createdAt, updatedAt) {

    fun update(title: String, content: String): Post {
        return copy(
            title = title,
            content = content
        )
    }
}
