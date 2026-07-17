package com.roky.kafkaaz.config

import javax.crypto.SecretKey
import javax.crypto.spec.SecretKeySpec
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.security.config.web.server.ServerHttpSecurity
import org.springframework.security.config.web.server.invoke
import org.springframework.security.crypto.factory.PasswordEncoderFactories
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.security.oauth2.jose.jws.MacAlgorithm
import org.springframework.security.oauth2.jwt.JwtEncoder
import org.springframework.security.oauth2.jwt.JwtValidators
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder
import org.springframework.security.oauth2.jwt.NimbusReactiveJwtDecoder
import org.springframework.security.oauth2.jwt.ReactiveJwtDecoder
import org.springframework.security.web.server.SecurityWebFilterChain
import org.springframework.security.web.server.util.matcher.ServerWebExchangeMatchers.pathMatchers
import org.springframework.web.server.ServerWebExchange
import reactor.core.publisher.Mono

@Configuration
@EnableConfigurationProperties(JwtProperties::class)
class SecurityConfig {

    @Bean
    fun securityWebFilterChain(http: ServerHttpSecurity): SecurityWebFilterChain {
        return http {
            csrf { disable() }
            httpBasic { disable() }
            formLogin { disable() }
            authorizeExchange {
                authorize("/api/v1/auth/login", permitAll)
                authorize("/v3/api-docs/**", permitAll)
                authorize("/swagger-ui.html", permitAll)
                authorize("/swagger-ui/**", permitAll)
                authorize(pathMatchers(HttpMethod.GET, "/api/v1/posts/**"), permitAll)
                authorize(anyExchange, authenticated)
            }
            oauth2ResourceServer {
                jwt { }
                authenticationEntryPoint = org.springframework.security.web.server.ServerAuthenticationEntryPoint { exchange, _ ->
                    writeError(exchange, HttpStatus.UNAUTHORIZED, "Authentication required")
                }
            }
            exceptionHandling {
                accessDeniedHandler = org.springframework.security.web.server.authorization.ServerAccessDeniedHandler { exchange, _ ->
                    writeError(exchange, HttpStatus.FORBIDDEN, "Access denied")
                }
            }
        }
    }

    @Bean
    fun passwordEncoder(): PasswordEncoder {
        return PasswordEncoderFactories.createDelegatingPasswordEncoder()
    }

    @Bean
    fun jwtEncoder(jwtProperties: JwtProperties): JwtEncoder {
        return NimbusJwtEncoder.withSecretKey(secretKey(jwtProperties))
            .algorithm(MacAlgorithm.HS256)
            .build()
    }

    @Bean
    fun reactiveJwtDecoder(jwtProperties: JwtProperties): ReactiveJwtDecoder {
        val decoder = NimbusReactiveJwtDecoder.withSecretKey(secretKey(jwtProperties))
            .macAlgorithm(MacAlgorithm.HS256)
            .build()
        decoder.setJwtValidator(JwtValidators.createDefaultWithIssuer(jwtProperties.issuer))
        return decoder
    }

    private fun secretKey(jwtProperties: JwtProperties): SecretKey {
        return SecretKeySpec(jwtProperties.secret.toByteArray(Charsets.UTF_8), "HmacSHA256")
    }

    private fun writeError(exchange: ServerWebExchange, status: HttpStatus, message: String): Mono<Void> {
        val response = exchange.response
        response.statusCode = status
        response.headers.contentType = MediaType.APPLICATION_JSON
        val body = """{"status":${status.value()},"message":"$message"}"""
        val buffer = response.bufferFactory().wrap(body.toByteArray(Charsets.UTF_8))
        return response.writeWith(Mono.just(buffer))
    }
}
