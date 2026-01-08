package com.rokyai.springaipoc.chat.controller

import com.ninjasquad.springmockk.MockkBean
import com.rokyai.springaipoc.chat.dto.ChatRequest
import com.rokyai.springaipoc.chat.dto.ChatResponse
import com.rokyai.springaipoc.chat.service.ChatService
import io.mockk.coEvery
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest
import org.springframework.http.MediaType
import org.springframework.test.web.reactive.server.WebTestClient

/**
 * ChatController 통합 테스트
 */
@WebFluxTest(ChatController::class)
@DisplayName("ChatController 통합 테스트")
class ChatControllerTest {

    @Autowired
    private lateinit var webTestClient: WebTestClient

    @MockkBean
    private lateinit var chatService: ChatService

    @Test
    @DisplayName("정상적인 채팅 요청 시 200 OK 응답 테스트")
    fun chatSuccess() = runTest {
        // Given - 사용자 메시지와 예상 응답 준비
        val request = ChatRequest(message = "안녕하세요!")
        val expectedResponse = ChatResponse(message = "안녕하세요! 무엇을 도와드릴까요?")

        coEvery { chatService.chat(request) } returns expectedResponse

        // When - POST /api/v1/chat 호출
        // Then - 응답 검증
        webTestClient.post()
            .uri("/api/v1/chat")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(request)
            .exchange()
            .expectStatus().isOk
            .expectBody()
            .jsonPath("$.message").isEqualTo(expectedResponse.message)
    }

    @Test
    @DisplayName("빈 메시지 전송 시 400 Bad Request 응답 테스트")
    fun chatWithBlankMessage() = runTest {
        // Given - 빈 메시지 전송
        val request = ChatRequest(message = "")

        // When - POST /api/v1/chat 호출
        // Then - 400 Bad Request 응답 검증
        webTestClient.post()
            .uri("/api/v1/chat")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(request)
            .exchange()
            .expectStatus().isBadRequest
    }

    @Test
    @DisplayName("공백만 있는 메시지 전송 시 400 Bad Request 응답 테스트")
    fun chatWithWhitespaceMessage() = runTest {
        // Given - 공백만 있는 메시지 전송
        val request = ChatRequest(message = "   ")

        // When - POST /api/v1/chat 호출
        // Then - 400 Bad Request 응답 검증
        webTestClient.post()
            .uri("/api/v1/chat")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(request)
            .exchange()
            .expectStatus().isBadRequest
    }

    @Test
    @DisplayName("서비스에서 예외 발생 시 500 Internal Server Error 응답 테스트")
    fun chatServiceThrowsException() = runTest {
        // Given - 서비스에서 예외 발생 설정
        val request = ChatRequest(message = "테스트 메시지")

        coEvery { chatService.chat(request) } throws IllegalStateException("ChatGPT 응답 생성 실패")

        // When - POST /api/v1/chat 호출
        // Then - 500 Internal Server Error 응답 검증
        webTestClient.post()
            .uri("/api/v1/chat")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(request)
            .exchange()
            .expectStatus().is5xxServerError
            .expectBody()
            .jsonPath("$.status").isEqualTo(500)
            .jsonPath("$.error").isEqualTo("Internal Server Error")
            .jsonPath("$.message").exists()
    }

    @Test
    @DisplayName("긴 메시지 전송 시 정상 동작 테스트")
    fun chatWithLongMessage() = runTest {
        // Given - 긴 메시지 준비
        val longMessage = "이것은 매우 긴 메시지입니다. ".repeat(100)
        val request = ChatRequest(message = longMessage)
        val expectedResponse = ChatResponse(message = "긴 메시지를 잘 받았습니다.")

        coEvery { chatService.chat(request) } returns expectedResponse

        // When - POST /api/v1/chat 호출
        // Then - 200 OK 응답 검증
        webTestClient.post()
            .uri("/api/v1/chat")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(request)
            .exchange()
            .expectStatus().isOk
            .expectBody()
            .jsonPath("$.message").isEqualTo(expectedResponse.message)
    }
}
