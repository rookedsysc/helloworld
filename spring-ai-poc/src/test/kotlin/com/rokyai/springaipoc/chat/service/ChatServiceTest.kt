package com.rokyai.springaipoc.chat.service

import com.rokyai.springaipoc.chat.dto.ChatRequest
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.ai.chat.model.ChatModel
import org.springframework.ai.chat.model.ChatResponse
import org.springframework.ai.chat.model.Generation
import org.springframework.ai.chat.prompt.Prompt
import org.springframework.ai.openai.api.OpenAiApi

/**
 * ChatService 단위 테스트
 */
@DisplayName("ChatService 테스트")
class ChatServiceTest {

    private val chatModel: ChatModel = mockk()
    private val chatService = ChatService(chatModel)

    @Test
    @DisplayName("정상적인 메시지 전송 및 응답 수신 테스트")
    fun chatSuccess() = runTest {
        // Given - ChatGPT에게 전송할 메시지 준비
        val request = ChatRequest(message = "안녕하세요!")
        val expectedResponse = "안녕하세요! 무엇을 도와드릴까요?"

        val mockGeneration = mockk<Generation>()
        val mockOutput = mockk<OpenAiApi.ChatCompletion.Choice.Message>()
        val mockChatResponse = mockk<ChatResponse>()

        every { mockOutput.content } returns expectedResponse
        every { mockGeneration.output } returns mockOutput
        every { mockChatResponse.result } returns mockGeneration
        every { chatModel.call(any<Prompt>()) } returns mockChatResponse

        // When - ChatService를 통해 메시지 전송
        val response = chatService.chat(request)

        // Then - 응답이 정상적으로 반환되는지 검증
        assertNotNull(response)
        assertEquals(expectedResponse, response.message)
    }

    @Test
    @DisplayName("ChatGPT 응답 생성 실패 시 예외 발생 테스트")
    fun chatFailure() = runTest {
        // Given - 응답 생성에 실패하는 상황 설정
        val request = ChatRequest(message = "테스트 메시지")
        val mockChatResponse = mockk<ChatResponse>()

        every { mockChatResponse.result } returns null
        every { chatModel.call(any<Prompt>()) } returns mockChatResponse

        // When & Then - 예외가 발생하는지 검증
        val exception = assertThrows(IllegalStateException::class.java) {
            runTest {
                chatService.chat(request)
            }
        }

        assertEquals("ChatGPT 응답 생성 실패", exception.message)
    }

    @Test
    @DisplayName("빈 메시지 전송 시 정상 동작 테스트")
    fun chatWithEmptyMessage() = runTest {
        // Given - 빈 메시지 전송
        val request = ChatRequest(message = "")
        val expectedResponse = "죄송하지만, 메시지를 이해하지 못했습니다."

        val mockGeneration = mockk<Generation>()
        val mockOutput = mockk<OpenAiApi.ChatCompletion.Choice.Message>()
        val mockChatResponse = mockk<ChatResponse>()

        every { mockOutput.content } returns expectedResponse
        every { mockGeneration.output } returns mockOutput
        every { mockChatResponse.result } returns mockGeneration
        every { chatModel.call(any<Prompt>()) } returns mockChatResponse

        // When - 빈 메시지 전송
        val response = chatService.chat(request)

        // Then - 응답이 정상적으로 반환되는지 검증
        assertNotNull(response)
        assertEquals(expectedResponse, response.message)
    }
}
