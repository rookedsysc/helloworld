package com.rookedsysc.order.config

import com.rookedsysc.order.infrastructure.out.point.PointApiClient
import com.rookedsysc.order.infrastructure.out.product.ProductApiClient
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.client.RestClient

@Configuration
class ApiClientConfig {

    @Bean
    fun productApiClient(): ProductApiClient {
        return ProductApiClient(
            RestClient.builder()
                .baseUrl("http://localhost:9785")
                .build()
        )
    }

    @Bean
    fun pointApiClient(): PointApiClient {
        return PointApiClient(
            RestClient.builder()
                .baseUrl("http://localhost:9787")
                .build()
        )
    }
}
