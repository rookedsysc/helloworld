package com.roky.kafkaaz.notification

import java.time.Instant

data class Notification(
    val id: Long? = null,
    val recipientMemberId: Long,
    val actorMemberId: Long,
    val postId: Long,
    val commentId: Long,
    val content: String,
    val createdAt: Instant? = null
)
