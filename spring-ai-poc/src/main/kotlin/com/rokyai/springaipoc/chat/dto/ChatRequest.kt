package com.rokyai.springaipoc.chat.dto

import jakarta.validation.constraints.NotBlank

/**
 * ChatGPT에게 전송할 메시지 요청 DTO
 *
 * @property message 사용자가 ChatGPT에게 보낼 메시지 (필수값, 공백 불가)
 */
data class ChatRequest(
    @field:NotBlank(message = "메시지는 비어있을 수 없습니다.")
    val message: String
)
