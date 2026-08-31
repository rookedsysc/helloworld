package com.roky.opensearchpoc

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.server.LocalServerPort
import org.springframework.http.HttpStatus
import org.springframework.web.client.RestClient

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class OpenSearchPocApplicationTests {

    @LocalServerPort
    private var 서버_포트: Int = 0

    @Autowired
    private lateinit var restClient: RestClient

    @Test
    @DisplayName("애플리케이션 컨텍스트가 기동되며 RestClient 빈이 등록된다")
    fun 컨텍스트_기동() {
        // Given - @SpringBootTest 가 컨텍스트를 기동한다

        // When - 주입된 RestClient 빈을 확인한다
        val 등록된_RestClient = restClient

        // Then - 빈이 정상적으로 주입되어 있다
        assertThat(등록된_RestClient).isNotNull()
    }

    @Test
    @DisplayName("springdoc 이 OpenAPI 문서를 노출하며 설정한 title 이 반영된다")
    fun OpenAPI_문서_노출() {
        // Given - springdoc 설정이 적용된 애플리케이션이 기동되어 있다

        // When - 애플리케이션의 RestClient 로 OpenAPI 문서 엔드포인트를 호출한다
        val 응답 = restClient.get()
            .uri("http://localhost:$서버_포트/v3/api-docs")
            .retrieve()
            .toEntity(String::class.java)

        // Then - 200 과 함께 설정한 문서 제목이 내려온다
        assertThat(응답.statusCode).isEqualTo(HttpStatus.OK)
        assertThat(응답.body).contains("OpenSearch PoC API")
    }
}
