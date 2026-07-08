package com.roky.kafkaaz

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class KafkaAzApplication

fun main(args: Array<String>) {
    runApplication<KafkaAzApplication>(*args)
}
