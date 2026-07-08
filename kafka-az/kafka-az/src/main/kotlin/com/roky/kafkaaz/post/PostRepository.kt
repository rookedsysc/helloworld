package com.roky.kafkaaz.post

import org.springframework.data.repository.reactive.ReactiveCrudRepository
import reactor.core.publisher.Flux

interface PostRepository : ReactiveCrudRepository<Post, Long> {

    fun findAllByOrderByIdDesc(): Flux<Post>
}
