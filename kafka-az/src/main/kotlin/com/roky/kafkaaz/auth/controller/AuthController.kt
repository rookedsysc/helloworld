package com.roky.kafkaaz.auth.controller

import com.roky.kafkaaz.auth.dto.LoginRequest
import com.roky.kafkaaz.auth.dto.LoginResponse
import com.roky.kafkaaz.auth.service.AuthService
import jakarta.validation.Valid
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import reactor.core.publisher.Mono

@RestController
@RequestMapping("/api/v1/auth")
class AuthController(
    private val authService: AuthService
) : AuthControllerDocs {

    @PostMapping("/login")
    override fun login(@Valid @RequestBody request: LoginRequest): Mono<LoginResponse> {
        return authService.login(request)
    }
}
