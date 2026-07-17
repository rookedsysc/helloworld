package com.roky.kafkaaz.auth

import com.roky.kafkaaz.member.Member
import com.roky.kafkaaz.member.MemberRepository
import org.springframework.http.HttpStatus
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.web.server.ResponseStatusException
import reactor.core.publisher.Mono
import reactor.core.scheduler.Schedulers

@Service
class AuthService(
    private val memberRepository: MemberRepository,
    private val passwordEncoder: PasswordEncoder,
    private val jwtTokenService: JwtTokenService
) {

    fun login(request: LoginRequest): Mono<LoginResponse> {
        return memberRepository.findByLoginId(request.id)
            .flatMap { member -> authenticate(member, request.password) }
            .switchIfEmpty(register(request))
    }

    private fun authenticate(member: Member, rawPassword: String): Mono<LoginResponse> {
        return Mono.fromCallable { passwordEncoder.matches(rawPassword, member.password) }
            .subscribeOn(Schedulers.boundedElastic())
            .flatMap { matches ->
                if (!matches) {
                    Mono.error(invalidCredentials())
                } else {
                    Mono.just(toResponse(member, false))
                }
            }
    }

    private fun register(request: LoginRequest): Mono<LoginResponse> {
        return Mono.fromCallable { passwordEncoder.encode(request.password) }
            .subscribeOn(Schedulers.boundedElastic())
            .map { encodedPassword -> Member(loginId = request.id, password = encodedPassword) }
            .flatMap(memberRepository::create)
            .map { member -> toResponse(member, true) }
    }

    private fun toResponse(member: Member, isNewMember: Boolean): LoginResponse {
        return LoginResponse(
            accessToken = jwtTokenService.createAccessToken(member),
            isNewMember = isNewMember
        )
    }

    private fun invalidCredentials(): ResponseStatusException {
        return ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid credentials")
    }
}
