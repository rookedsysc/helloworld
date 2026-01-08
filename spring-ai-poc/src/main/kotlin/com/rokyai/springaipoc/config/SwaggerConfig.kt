package com.rokyai.springaipoc.config

import io.swagger.v3.oas.models.OpenAPI
import io.swagger.v3.oas.models.info.Info
import org.slf4j.LoggerFactory
import org.springdoc.core.utils.SpringDocUtils
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.server.WebSession

@Configuration
class SwaggerConfig() {

    private val logger = LoggerFactory.getLogger(SwaggerConfig::class.java)

    init {
        SpringDocUtils.getConfig().addRequestWrapperToIgnore(
            WebSession::class.java,
        )
    }

    @Bean
    fun openApi(): OpenAPI {
        return OpenAPI()
            .info(
                Info()
                    .title("Spring AI POC")
                    .description("Spring AI + WebFlux + R2DBC 공부")
            )
    }
}

