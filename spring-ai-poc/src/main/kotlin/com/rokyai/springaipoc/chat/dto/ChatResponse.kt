package com.rokyai.springaipoc.chat.dto

/**
 * ChatGPT로부터 받은 응답 DTO
 *
 * @property message ChatGPT가 생성한 응답 메시지
 */
data class ChatResponse(
    val message: String
)
