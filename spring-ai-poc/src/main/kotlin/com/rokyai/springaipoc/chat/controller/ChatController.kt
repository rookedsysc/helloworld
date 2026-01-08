package com.rokyai.springaipoc.chat.controller

import com.rokyai.springaipoc.chat.dto.ChatRequest
import com.rokyai.springaipoc.chat.dto.ChatResponse
import com.rokyai.springaipoc.chat.service.ChatService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.*
import reactor.core.publisher.Flux

/**
 * ChatGPT와 대화하는 API Controller
 *
 * OpenAI의 ChatGPT 모델을 활용하여 사용자 메시지에 대한 응답을 생성합니다.
 * WebFlux 환경에서 Coroutines를 사용하여 비동기 Non-blocking 방식으로 동작합니다.
 */
@RestController
@RequestMapping("/api/v1/chat")
@Tag(name = "Chat API", description = "ChatGPT와 대화하는 API")
class ChatController(
    private val chatService: ChatService
) {
    /**
     * ChatGPT에게 메시지를 전송하고 응답을 받습니다.
     *
     * @param request 사용자가 보낼 메시지를 포함한 요청 객체
     * @return ChatGPT의 응답 메시지
     */
    @PostMapping
    @ResponseStatus(HttpStatus.OK)
    @Operation(
        summary = "ChatGPT와 대화",
        description = "사용자 메시지를 ChatGPT에게 전송하고 응답을 받습니다."
    )
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "정상적으로 응답을 받았습니다.",
                content = [Content(schema = Schema(implementation = ChatResponse::class))]
            ),
            ApiResponse(
                responseCode = "400",
                description = "잘못된 요청입니다. 메시지가 비어있거나 형식이 올바르지 않습니다.",
                content = [Content()]
            ),
            ApiResponse(
                responseCode = "500",
                description = "서버 내부 오류가 발생했습니다. ChatGPT API 호출 실패 또는 응답 생성 실패",
                content = [Content()]
            )
        ]
    )
    suspend fun chat(@Valid @RequestBody request: ChatRequest): ChatResponse {
        return chatService.chat(request)
    }

    /**
     * ChatGPT에게 메시지를 전송하고 스트리밍 방식으로 응답을 받습니다.
     *
     * SSE (Server-Sent Events)를 통해 실시간으로 생성되는 텍스트를 전송합니다.
     * 전체 응답이 완료될 때까지 기다리지 않고 생성되는 즉시 텍스트를 스트리밍합니다.
     *
     * @param request 사용자가 보낼 메시지를 포함한 요청 객체
     * @return ChatGPT의 응답 메시지 스트림 (text/event-stream)
     */
    @PostMapping("/stream", produces = [MediaType.TEXT_EVENT_STREAM_VALUE])
    @Operation(
        summary = "ChatGPT와 스트리밍 대화",
        description = "사용자 메시지를 ChatGPT에게 전송하고 SSE를 통해 실시간으로 응답을 받습니다. " +
                "응답이 생성되는 즉시 클라이언트로 전송됩니다."
    )
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "정상적으로 스트리밍 응답을 시작했습니다.",
                content = [Content(mediaType = MediaType.TEXT_EVENT_STREAM_VALUE)]
            ),
            ApiResponse(
                responseCode = "400",
                description = "잘못된 요청입니다. 메시지가 비어있거나 형식이 올바르지 않습니다.",
                content = [Content()]
            ),
            ApiResponse(
                responseCode = "500",
                description = "서버 내부 오류가 발생했습니다. ChatGPT API 호출 실패 또는 스트리밍 오류",
                content = [Content()]
            )
        ]
    )
    fun chatStream(@Valid @RequestBody request: ChatRequest): Flux<String> {
        return chatService.chatStream(request)
    }
}
