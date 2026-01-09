package com.rokyai.springaipoc.chat.entity

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table
import java.time.LocalDateTime
import java.time.OffsetDateTime
import java.time.OffsetTime
import java.time.ZoneOffset
import java.util.TimeZone
import java.util.UUID

/**
 * 채팅 히스토리 엔티티
 * 사용자와 AI 어시스턴트 간의 대화 내용을 저장하는 테이블
 */
@Table("chat_history")
data class ChatHistory(
    /**
     * 채팅 히스토리 고유 ID (UUID)
     */
    @Id
    val id: UUID? = null,

    /**
     * 사용자가 입력한 메시지
     */
    @Column("user_message")
    val userMessage: String,

    /**
     * AI 어시스턴트가 생성한 응답 메시지
     */
    @Column("assistant_message")
    val assistantMessage: String,

    /**
     * 채팅 히스토리 생성 시간 (UTC)
     */
    @Column("created_at")
    val createdAt: OffsetDateTime = OffsetDateTime.now(ZoneOffset.UTC),
)
