package com.roky.kafkaaz.auth.service

import com.roky.kafkaaz.config.JwtProperties
import com.roky.kafkaaz.member.domain.Member
import java.time.Instant
import org.springframework.security.oauth2.jose.jws.MacAlgorithm
import org.springframework.security.oauth2.jwt.JwtClaimsSet
import org.springframework.security.oauth2.jwt.JwtEncoder
import org.springframework.security.oauth2.jwt.JwtEncoderParameters
import org.springframework.security.oauth2.jwt.JwsHeader
import org.springframework.stereotype.Service

@Service
class JwtTokenService(
    private val jwtEncoder: JwtEncoder,
    private val jwtProperties: JwtProperties
) {

    fun createAccessToken(member: Member): String {
        val now = Instant.now()
        val claims = JwtClaimsSet.builder()
            .issuer(jwtProperties.issuer)
            .issuedAt(now)
            .expiresAt(now.plusSeconds(jwtProperties.accessTokenExpirationSeconds))
            .subject(requireNotNull(member.id).toString())
            .build()
        val header = JwsHeader.with(MacAlgorithm.HS256).build()

        return jwtEncoder.encode(JwtEncoderParameters.from(header, claims)).tokenValue
    }
}
