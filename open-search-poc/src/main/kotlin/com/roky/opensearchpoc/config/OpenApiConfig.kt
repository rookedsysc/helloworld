package com.roky.opensearchpoc.config

import io.swagger.v3.oas.models.OpenAPI
import io.swagger.v3.oas.models.info.Info
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class OpenApiConfig {

    @Bean
    fun openSearchPocOpenApi(): OpenAPI = OpenAPI()
        .info(
            Info()
                .title("OpenSearch PoC API")
                .description("OpenSearch 검색 기능 검증용 PoC API 문서")
                .version("v1"),
        )
}
