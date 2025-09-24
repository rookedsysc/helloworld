package com.rookedsysc.monolithic

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class MonolithicApplication

fun main(args: Array<String>) {
    runApplication<MonolithicApplication>(*args)
}
