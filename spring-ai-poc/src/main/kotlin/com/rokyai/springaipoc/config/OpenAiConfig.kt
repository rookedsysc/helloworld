package com.rokyai.springaipoc.config

import org.springframework.ai.chat.model.ChatModel
import org.springframework.ai.openai.OpenAiChatModel
import org.springframework.ai.openai.OpenAiChatOptions
import org.springframework.ai.openai.api.OpenAiApi
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

/**
 * OpenAI ChatModel 수동 구성
 *
 * Spring AI의 자동 구성이 작동하지 않는 경우를 대비하여
 * ChatModel 빈을 수동으로 생성합니다.
 */
@Configuration
class OpenAiConfig {

    /**
     * OpenAI API 클라이언트를 생성합니다.
     *
     * @param apiKey OpenAI API 키 (application.yml에서 주입)
     * @return OpenAiApi 인스턴스
     */
    @Bean
    fun openAiApi(
        @Value("\${spring.ai.openai.api-key}") apiKey: String
    ): OpenAiApi {
        return OpenAiApi.builder()
            .apiKey(apiKey)
            .build()
    }

    /**
     * OpenAI ChatModel 옵션을 구성합니다.
     *
     * @param model 사용할 OpenAI 모델 (기본값: gpt-4o-mini)
     * @param temperature 응답의 창의성 수준 (기본값: 0.7, 범위: 0.0 ~ 2.0)
     * @return OpenAiChatOptions 인스턴스
     */
    @Bean
    fun openAiChatOptions(
        @Value("\${spring.ai.openai.chat.options.model:gpt-4o-mini}") model: String,
        @Value("\${spring.ai.openai.chat.options.temperature:0.7}") temperature: Double
    ): OpenAiChatOptions {
        return OpenAiChatOptions.builder()
            .model(model)
            .temperature(temperature)
            .build()
    }

    /**
     * ChatModel 빈을 생성합니다.
     *
     * OpenAiApi와 OpenAiChatOptions를 사용하여
     * OpenAiChatModel 인스턴스를 생성합니다.
     *
     * @param openAiApi OpenAI API 클라이언트
     * @param options ChatModel 옵션 (모델, temperature 등)
     * @return ChatModel 인스턴스
     */
    @Bean
    fun chatModel(
        openAiApi: OpenAiApi,
        options: OpenAiChatOptions
    ): ChatModel {
        return OpenAiChatModel.builder()
            .openAiApi(openAiApi)
            .defaultOptions(options)
            .build()
    }
}
