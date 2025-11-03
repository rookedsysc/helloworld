package com.rookedsysc.product

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.annotation.EnableAspectJAutoProxy

@SpringBootApplication
@EnableAspectJAutoProxy
class ProductApplication

fun main(args: Array<String>) {
    runApplication<ProductApplication>(*args)
}
