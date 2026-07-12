package com.roky.kafkaaz.support

import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import org.testcontainers.containers.PostgreSQLContainer
import org.testcontainers.utility.DockerImageName

abstract class PostgreSQLTestContainerSupport {

    companion object {
        private const val DATABASE_NAME = "kafka_az_test"

        private val postgres = PostgreSQLContainer(DockerImageName.parse("postgres:17-alpine"))
            .withDatabaseName(DATABASE_NAME)
            .withUsername("kafka_az")
            .withPassword("kafka_az")
            .withInitScript("schema.sql")

        init {
            postgres.start()
        }

        @JvmStatic
        @DynamicPropertySource
        fun registerPostgreSQLProperties(registry: DynamicPropertyRegistry) {
            registry.add("spring.r2dbc.url") {
                "r2dbc:postgresql://${postgres.host}:${postgres.firstMappedPort}/$DATABASE_NAME"
            }
            registry.add("spring.r2dbc.username", postgres::getUsername)
            registry.add("spring.r2dbc.password", postgres::getPassword)
        }
    }
}
