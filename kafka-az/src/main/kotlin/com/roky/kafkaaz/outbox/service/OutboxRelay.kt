package com.roky.kafkaaz.outbox.service

import com.roky.kafkaaz.outbox.domain.OutboxEvent
import com.roky.kafkaaz.outbox.repository.OutboxRepository
import org.slf4j.LoggerFactory
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import reactor.core.publisher.Mono

@Component
@ConditionalOnProperty(prefix = "outbox.relay", name = ["enabled"], havingValue = "true", matchIfMissing = true)
class OutboxRelay(
    private val outboxRepository: OutboxRepository,
    private val kafkaTemplate: KafkaTemplate<String, String>,
    private val outboxRelayLock: OutboxRelayLock
) {

    @Scheduled(fixedDelayString = "\${outbox.relay.delay-ms:1000}")
    fun publishPendingEvents() {
        if (!outboxRelayLock.tryAcquire()) {
            return
        }

        outboxRepository.findPending(BATCH_SIZE)
            .concatMap(::publish)
            .then()
            .doFinally { outboxRelayLock.release() }
            .subscribe(
                {},
                { error -> logger.error("Outbox relay query failed", error) }
            )
    }

    private fun publish(event: OutboxEvent): Mono<Void> {
        return Mono.fromFuture(
            kafkaTemplate.send(TOPIC, event.aggregateId.toString(), event.payload.data())
        )
            .then(outboxRepository.markPublished(event.id))
            .onErrorResume { error ->
                logger.error("Outbox event publish failed. eventId={}", event.id, error)
                Mono.empty()
            }
    }

    private companion object {
        private const val TOPIC = "post.published.v1"
        private const val BATCH_SIZE = 100
        private val logger = LoggerFactory.getLogger(OutboxRelay::class.java)
    }
}
