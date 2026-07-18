package com.roky.kafkaaz.config

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties("jwt")
data class JwtProperties(
    val issuer: String,
    val secret: String,
    val accessTokenExpirationSeconds: Long
)
