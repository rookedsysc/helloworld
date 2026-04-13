package com.roky.otelexamplespringjava

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class OtelExampleSpringJavaApplication

fun main(args: Array<String>) {
    runApplication<OtelExampleSpringJavaApplication>(*args)
}
