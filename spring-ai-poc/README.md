# Spring AI POC - ChatGPT 인사 API

Spring AI를 활용하여 OpenAI의 ChatGPT와 통신하는 RESTful API 프로젝트입니다.

## 기술 스택

- **Language**: Kotlin 2.2.21
- **Framework**: Spring Boot 4.0.1 (WebFlux)
- **AI Integration**: Spring AI 1.0.0-M4
- **Build Tool**: Gradle (Kotlin DSL)
- **Java Version**: 21
- **Documentation**: SpringDoc OpenAPI (Swagger)

## 프로젝트 구조

```
src/main/kotlin/com/rokyai/springaipoc/
├── chat/
│   ├── controller/
│   │   └── ChatController.kt          # ChatGPT API 엔드포인트
│   ├── service/
│   │   └── ChatService.kt             # ChatGPT 통신 비즈니스 로직
│   ├── dto/
│   │   ├── ChatRequest.kt             # 요청 DTO
│   │   └── ChatResponse.kt            # 응답 DTO
│   └── exception/
│       ├── ErrorResponse.kt           # 공통 에러 응답 DTO
│       └── GlobalExceptionHandler.kt  # 전역 예외 처리 핸들러
└── helloworld/
    └── controller/
        └── HelloWorldController.kt     # Hello World 테스트 엔드포인트
```

## 환경 설정

### 1. OpenAI API 키 설정

`.env` 파일을 프로젝트 루트에 생성하고 OpenAI API 키를 설정합니다:

```env
OPEN_AI=your-openai-api-key-here
```

### 2. application.yml 설정

`src/main/resources/application.yml`에서 Spring AI 설정을 확인합니다:

```yaml
spring:
  ai:
    openai:
      api-key: ${OPEN_AI}
      chat:
        options:
          model: gpt-4o-mini
          temperature: 0.7
```

## API 명세

### ChatGPT 인사 API

#### POST /api/v1/chat

ChatGPT에게 메시지를 전송하고 응답을 받습니다.

**요청 (Request)**

```json
{
  "message": "안녕하세요!"
}
```

**응답 (Response)**

- **성공 (200 OK)**

```json
{
  "message": "안녕하세요! 무엇을 도와드릴까요?"
}
```

- **실패 (400 Bad Request)** - 빈 메시지 전송 시

```json
{
  "timestamp": "2024-01-08T12:00:00",
  "status": 400,
  "error": "Bad Request",
  "message": "메시지는 비어있을 수 없습니다.",
  "path": "/api/v1/chat"
}
```

- **실패 (500 Internal Server Error)** - ChatGPT API 호출 실패 시

```json
{
  "timestamp": "2024-01-08T12:00:00",
  "status": 500,
  "error": "Internal Server Error",
  "message": "ChatGPT 응답 생성 실패",
  "path": "/api/v1/chat"
}
```

## 실행 방법

### 1. 의존성 설치 및 빌드

```bash
./gradlew build
```

### 2. 애플리케이션 실행

```bash
./gradlew bootRun
```

또는 환경 변수와 함께 실행:

```bash
OPEN_AI=your-api-key ./gradlew bootRun
```

### 3. Swagger UI 접속

애플리케이션 실행 후 브라우저에서 다음 주소로 접속:

```
http://localhost:8080/swagger-ui.html
```

## 테스트

### 전체 테스트 실행

```bash
./gradlew test
```

### 테스트 커버리지

- **ChatServiceTest**: ChatService 단위 테스트
  - 정상적인 메시지 전송 및 응답 수신
  - ChatGPT 응답 생성 실패 시 예외 발생
  - 빈 메시지 전송 시 정상 동작

- **ChatControllerTest**: ChatController 통합 테스트
  - 정상적인 채팅 요청 시 200 OK 응답
  - 빈 메시지 전송 시 400 Bad Request 응답
  - 공백만 있는 메시지 전송 시 400 Bad Request 응답
  - 서비스에서 예외 발생 시 500 Internal Server Error 응답
  - 긴 메시지 전송 시 정상 동작

## 공통 코드

### ErrorResponse (공통 에러 응답 DTO)

**위치**: `src/main/kotlin/com/rokyai/springaipoc/chat/exception/ErrorResponse.kt`

**사용법**:
```kotlin
val errorResponse = ErrorResponse(
    status = 400,
    error = "Bad Request",
    message = "잘못된 요청입니다.",
    path = "/api/v1/chat"
)
```

**파라미터**:
- `timestamp`: 에러 발생 시각 (UTC 기준, 자동 설정)
- `status`: HTTP 상태 코드
- `error`: 에러 타입 (예: Bad Request, Internal Server Error)
- `message`: 에러 상세 메시지
- `path`: 에러가 발생한 API 경로

**반환값**: ErrorResponse 객체

**동작**: 모든 API 에러 응답에 대해 일관된 형식을 제공합니다.

### GlobalExceptionHandler (전역 예외 처리 핸들러)

**위치**: `src/main/kotlin/com/rokyai/springaipoc/chat/exception/GlobalExceptionHandler.kt`

**사용법**: 자동으로 적용되며, 애플리케이션 전체에서 발생하는 예외를 처리합니다.

**처리하는 예외**:
- `IllegalArgumentException`: 400 Bad Request 응답
- `IllegalStateException`: 500 Internal Server Error 응답
- `Exception`: 500 Internal Server Error 응답 (그 외 모든 예외)

**동작**: 예외 발생 시 ErrorResponse 형식으로 일관된 에러 응답을 반환합니다.

## 주의사항

1. `.env` 파일은 절대 Git에 커밋하지 마세요. `.gitignore`에 포함되어 있습니다.
2. OpenAI API 키는 반드시 환경 변수로 관리하세요.
3. API 사용량에 따라 OpenAI 비용이 발생할 수 있습니다.
4. WebFlux 환경에서 Coroutines를 사용하므로 모든 핸들러 함수는 `suspend` 키워드를 사용합니다.

## 라이선스

이 프로젝트는 MIT 라이선스를 따릅니다.
