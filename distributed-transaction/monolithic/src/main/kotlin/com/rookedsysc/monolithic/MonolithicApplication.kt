package com.rookedsysc.monolithic

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.annotation.EnableAspectJAutoProxy

@SpringBootApplication
@EnableAspectJAutoProxy
class MonolithicApplication

fun main(args: Array<String>) {
    runApplication<MonolithicApplication>(*args)
}
