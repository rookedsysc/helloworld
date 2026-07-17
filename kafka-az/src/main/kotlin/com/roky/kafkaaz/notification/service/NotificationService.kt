package com.roky.kafkaaz.notification.service

import com.roky.kafkaaz.notification.domain.Notification
import com.roky.kafkaaz.notification.repository.NotificationRepository
import org.springframework.stereotype.Service
import reactor.core.publisher.Mono

@Service
class NotificationService(
    private val notificationRepository: NotificationRepository
) {

    fun create(
        recipientMemberId: Long,
        actorMemberId: Long,
        postId: Long,
        commentId: Long,
        content: String
    ): Mono<Notification> {
        if (recipientMemberId == actorMemberId) {
            return Mono.empty()
        }

        return notificationRepository.createIfAbsent(
            Notification(
                recipientMemberId = recipientMemberId,
                actorMemberId = actorMemberId,
                postId = postId,
                commentId = commentId,
                content = content
            )
        )
    }
}
