package com.rookedsysc.monolithic.config.lock

import org.redisson.Redisson
import org.redisson.api.RedissonClient
import org.redisson.config.Config
import org.redisson.config.ConstantDelay
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import java.time.Duration

/**
 * Redisson 설정 클래스
 * Single Responsibility: Redis 연결 설정 및 Redisson 클라이언트 생성 책임만 담당
 * Dependency Inversion: RedissonClient 추상화에 의존
 */
@Configuration
class RedissonConfig {

    @Value("\${spring.data.redis.host}")
    private lateinit var redisHost: String

    @Value("\${spring.data.redis.port}")
    private var redisPort: Int = 26379

    @Value("\${spring.data.redis.password}")
    private lateinit var redisPassword: String

    @Bean
    fun redissonClient(): RedissonClient {
        val config = Config()
        config.useSingleServer()
            .setAddress("redis://$redisHost:$redisPort")
            .setPassword(redisPassword)
            .setConnectionPoolSize(64)
            .setConnectionMinimumIdleSize(10)
            .setConnectTimeout(10000)
            .setTimeout(3000)
            .setRetryAttempts(3)
            .setRetryDelay(ConstantDelay(Duration.ofMillis(1500)))

        return Redisson.create(config)
    }
}
