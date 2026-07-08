package com.roky.kafkaaz

import com.roky.kafkaaz.support.PostgreSQLTestContainerSupport
import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.SpringBootTest

@SpringBootTest
class KafkaAzApplicationTests : PostgreSQLTestContainerSupport() {

    @Test
    fun contextLoads() {
    }

}
