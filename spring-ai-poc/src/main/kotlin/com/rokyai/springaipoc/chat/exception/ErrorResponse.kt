package com.rokyai.springaipoc.chat.exception

import java.time.LocalDateTime

/**
 * API 에러 응답 DTO
 *
 * 모든 API 에러 응답에 사용되는 공통 DTO입니다.
 *
 * @property timestamp 에러 발생 시각 (UTC 기준)
 * @property status HTTP 상태 코드
 * @property error 에러 타입 (예: Bad Request, Internal Server Error)
 * @property message 에러 상세 메시지
 * @property path 에러가 발생한 API 경로
 */
data class ErrorResponse(
    val timestamp: LocalDateTime = LocalDateTime.now(),
    val status: Int,
    val error: String,
    val message: String,
    val path: String
)
