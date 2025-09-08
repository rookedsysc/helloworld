package com.rookedsysc.distributedtransaction

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class DistributedTransactionApplication

fun main(args: Array<String>) {
    runApplication<DistributedTransactionApplication>(*args)
}
