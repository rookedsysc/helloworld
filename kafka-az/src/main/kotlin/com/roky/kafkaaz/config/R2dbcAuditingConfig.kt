package com.roky.kafkaaz.config

import java.time.Instant
import java.util.Optional
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.auditing.DateTimeProvider
import org.springframework.data.r2dbc.config.EnableR2dbcAuditing

@Configuration
@EnableR2dbcAuditing(dateTimeProviderRef = "utcDateTimeProvider")
class R2dbcAuditingConfig {

    @Bean
    fun utcDateTimeProvider(): DateTimeProvider {
        return DateTimeProvider { Optional.of(Instant.now()) }
    }
}
