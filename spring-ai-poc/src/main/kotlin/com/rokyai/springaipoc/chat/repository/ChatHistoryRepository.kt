package com.rokyai.springaipoc.chat.repository

import com.rokyai.springaipoc.chat.entity.ChatHistory
import org.springframework.data.repository.reactive.ReactiveCrudRepository
import org.springframework.stereotype.Repository
import java.util.UUID

/**
 * 채팅 히스토리 레포지토리
 * 채팅 대화 내용을 데이터베이스에 저장하고 조회하는 인터페이스
 */
@Repository
interface ChatHistoryRepository : ReactiveCrudRepository<ChatHistory, UUID>
