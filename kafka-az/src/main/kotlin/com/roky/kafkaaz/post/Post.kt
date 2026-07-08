package com.roky.kafkaaz.post

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
    val content: String
) {

    fun update(title: String, content: String): Post {
        return copy(
            title = title,
            content = content
        )
    }
}
