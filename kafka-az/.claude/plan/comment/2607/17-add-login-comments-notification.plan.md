# 로그인 겸 회원가입, 댓글, 알림 생성 서비스 구현

## Test Case Decision
> 2026-07-17 사용자의 구현 요청으로 필수 테스트를 모두 승인했다.
- [x] Unit/behavior: 기존 회원이 잘못된 비밀번호로 로그인 요청 → 회원을 새로 만들지 않고 `401 Unauthorized` — Decision: Required — (지금 RED 이유: 인증 도메인이 없음)
- [x] Integration/contract: 신규 로그인 ID 요청 → 회원 생성, 해시 비밀번호 저장, 유효한 JWT 반환 — Decision: Required — (지금 RED 이유: 회원·JWT 구현이 없음)
- [x] Integration/contract: JWT 회원이 게시물에 댓글 작성 후 목록 조회 → 작성자와 게시물에 연결된 댓글을 오래된 순서로 반환 — Decision: Required — (지금 RED 이유: 댓글 도메인과 API가 없음)
- [x] Regression: 동일 회원·댓글 알림을 반복 생성 → 알림은 한 건만 유지 — Decision: Required — (지금 RED 이유: 알림 저장소와 중복 제약이 없음)

### Open Test Questions
- [x] 불확실성 없음

## Business Goal
POC에서 별도 회원가입 화면 없이 로그인 요청만으로 회원을 준비하고 JWT를 발급한다. 인증된 회원이 게시물을 작성하고 댓글을 남길 수 있게 하며, 향후 Kafka Consumer가 댓글 이벤트를 처리해 게시물 작성자에게 알림을 생성할 수 있도록 API 없는 알림 생성 서비스를 제공한다.

## Scope
- **In Scope**:
  - ID·비밀번호 기반 로그인 겸 자동 회원가입 API
  - POC용 애플리케이션 설정 파일 내 고정 JWT secret/issuer/만료 시간
  - 회원 1:N 게시물 관계와 게시물 작성자 권한 검사
  - 인증 회원의 댓글 작성 API
  - 게시물별 댓글 목록 조회 API
  - 댓글 ID를 중복 방지 키로 사용하는 알림 생성 Service와 Repository
  - Auth/Post/Comment API Swagger 문서와 Bearer JWT security scheme
  - 공통 API 에러 응답과 인증·인가 에러 처리
  - Testcontainers PostgreSQL 기반 통합 테스트 및 기존 Post 회귀 테스트
- **Out of Scope**:
  - 별도 회원가입 API, refresh token, 로그아웃, 역할/권한 등급
  - JWT 환경변수·외부 Secret Manager 연동과 운영용 키 회전
  - 댓글 수정·삭제, 대댓글, 댓글 페이지네이션
  - 알림 생성 API, 알림 목록 API, 읽음 처리
  - Kafka Consumer와 댓글 이벤트 발행
  - 게시물·댓글 화면 또는 다른 Frontend 작업

## Codebase Analysis Summary
현재 프로젝트는 Kotlin 2.3.21, Spring Boot 4.1.0, WebFlux, jOOQ, R2DBC PostgreSQL을 사용한다. `post` 패키지는 Controller → Service → Repository 구조이며 Repository가 jOOQ Publisher를 Reactor `Mono`/`Flux`로 감싼다. DB 스키마는 `src/main/resources/schema.sql`에서 초기화하고, 통합 테스트는 `PostgreSQLTestContainerSupport`와 `WebTestClient`를 사용한다. 현재 회원·인증·댓글·알림 도메인과 Spring Security 의존성은 없다.

### Relevant Files
| File | Role | Action |
|------|------|--------|
| `build.gradle.kts` | Spring Security와 JWT Resource Server 의존성 | Modify |
| `src/main/resources/application.yaml` | POC JWT issuer, secret, 만료 시간 | Modify |
| `src/test/resources/application.yaml` | 테스트용 보안·DB 설정 | Modify |
| `src/main/resources/schema.sql` | members/comments/notifications 및 posts.member_id 스키마 | Modify |
| `src/main/kotlin/com/roky/kafkaaz/config/SecurityConfig.kt` | WebFlux JWT 인증·인가 설정 | Create |
| `src/main/kotlin/com/roky/kafkaaz/config/JwtProperties.kt` | 고정 JWT 설정 바인딩 | Create |
| `src/main/kotlin/com/roky/kafkaaz/config/OpenApiConfig.kt` | OpenAPI 설명과 Bearer scheme | Modify |
| `src/main/kotlin/com/roky/kafkaaz/common/ApiErrorResponse.kt` | 공통 에러 응답 | Create |
| `src/main/kotlin/com/roky/kafkaaz/common/GlobalExceptionHandler.kt` | Validation·비즈니스 예외 변환 | Create |
| `src/main/kotlin/com/roky/kafkaaz/member/*` | 회원 모델·Repository | Create |
| `src/main/kotlin/com/roky/kafkaaz/auth/*` | 단일 로그인 API, JWT 발급, DTO, Swagger Docs | Create |
| `src/main/kotlin/com/roky/kafkaaz/post/Post.kt` | 작성자 회원 ID 추가 | Modify |
| `src/main/kotlin/com/roky/kafkaaz/post/PostRepository.kt` | member_id 저장·조회 | Modify |
| `src/main/kotlin/com/roky/kafkaaz/post/PostService.kt` | 작성자 연결과 소유권 검사 | Modify |
| `src/main/kotlin/com/roky/kafkaaz/post/PostController.kt` | JWT principal 전달과 Docs interface 적용 | Modify |
| `src/main/kotlin/com/roky/kafkaaz/post/PostResponse.kt` | memberId 응답 추가 | Modify |
| `src/main/kotlin/com/roky/kafkaaz/post/PostControllerDocs.kt` | Post Swagger 계약 | Create |
| `src/main/kotlin/com/roky/kafkaaz/comment/*` | 댓글 모델·DTO·Repository·Service·Controller·Docs | Create |
| `src/main/kotlin/com/roky/kafkaaz/notification/*` | 알림 모델·Repository·생성 Service | Create |
| `src/test/kotlin/com/roky/kafkaaz/auth/AuthControllerIntegrationTests.kt` | 자동 가입·로그인·JWT 계약 | Create |
| `src/test/kotlin/com/roky/kafkaaz/comment/CommentControllerIntegrationTests.kt` | 댓글 작성·목록 계약 | Create |
| `src/test/kotlin/com/roky/kafkaaz/notification/NotificationServiceIntegrationTests.kt` | 알림 생성·멱등성 계약 | Create |
| `src/test/kotlin/com/roky/kafkaaz/post/PostControllerIntegrationTests.kt` | 회원 연관과 작성자 권한 회귀 | Modify |

### Conventions to Follow
| Convention | Source | Rule |
|-----------|--------|------|
| 계층 구조 | 기존 `post` 패키지 | Controller → Service → Repository 책임을 분리하고 Reactor 타입을 유지한다. |
| 데이터 접근 | `PostRepository.kt` | 코드 생성 없는 jOOQ field/table 선언과 명시적 Record 매핑을 재사용한다. |
| DTO 이름 | `BACKEND.md` | 요청은 `Request`, 응답은 `Response` 접미사를 사용하고 Mapper 함수로 변환한다. |
| Swagger | `SPRING_BOOT_SWAGGER.md` | Controller 어노테이션을 별도 `{Controller}Docs` interface로 이동하고 한국어 summary/description/error를 작성한다. |
| 테스트 | 기존 통합 테스트와 `SPRING_BOOT.md` | Testcontainers, WebTestClient, Given-When-Then 구조로 실제 HTTP·DB 결과를 검증한다. |
| 변경 범위 | `CODE_PRINCIPLES.md` | 요청 범위만 최소 변경하고 단일 사용 추상화나 미래 기능을 추가하지 않는다. |

## Architecture Decisions
| Decision | Choice | Rationale | Alternatives |
|----------|--------|-----------|--------------|
| 로그인과 회원가입 | `POST /api/v1/auth/login`에서 로그인 ID 부재 시 자동 가입 | POC의 단일 진입점 요구를 충족한다. | 회원가입·로그인 API 분리 |
| 비밀번호 저장 | Spring Security `PasswordEncoder`의 adaptive one-way hash | 평문 저장을 피하고 로그인 시 안전하게 비교한다. | 평문, 직접 해시 구현 |
| JWT | HMAC secret을 `application.yaml`에 고정하고 access token만 발급 | 사용자가 환경변수를 제외한 POC 구성을 명시했다. | RSA 키 쌍, 환경변수, 외부 IdP |
| JWT principal | `sub`에 DB 회원 PK 문자열 저장 | 게시물·댓글 작성자를 요청 필드가 아닌 인증 주체에서 결정하고 로그인 ID와 PK를 구분한다. | 로그인 ID claim을 PK처럼 사용 |
| Post 소유권 | `members 1:N posts`, 생성 인증 필수, 수정·삭제 작성자 한정 | 회원 연관관계를 실제 인증·인가 규칙으로 보장한다. | memberId 요청 파라미터 허용 |
| 댓글 조회 순서 | `created_at ASC, id ASC` | 대화 흐름을 오래된 댓글부터 안정적으로 반환한다. | 최신순 |
| 알림 호출 경계 | 댓글 API와 직접 연결하지 않고 `NotificationService`만 제공 | 향후 Kafka Consumer가 댓글 이벤트를 처리하는 경계를 보존한다. | 댓글 트랜잭션에서 즉시 생성 |
| 알림 멱등성 | `(recipient_member_id, comment_id)` unique | 댓글 하나에 대한 회원별 알림은 최대 한 건이라는 현재 계약을 단순하게 보장한다. | 별도 event UUID |
| 자기 알림 | `actorMemberId == recipientMemberId`이면 저장하지 않음 | 자신의 게시물에 직접 쓴 댓글로 자기 알림을 만들지 않는다. | 자기 알림도 저장 |
| 알림 API | 없음 | 사용자가 생성 Service만 요청했다. | 생성·목록·읽음 API |

## API Contracts

### POST `/api/v1/auth/login`
- Headers: `Content-Type: application/json`
- Request: `{ "id": "roky", "password": "password123" }`
- Validation: id 공백 불가·최대 50자, password 8~100자
- Success: `200 OK`, `{ "accessToken": "...", "tokenType": "Bearer", "isNewMember": true|false }`
- Errors: `400` validation, `401` 기존 회원 비밀번호 불일치
- Note: 로그인 ID가 없으면 회원 생성 후 `isNewMember=true`, 있으면 검증 후 `false`

### POST `/api/v1/posts`
- Headers: `Authorization: Bearer <JWT>`, `Content-Type: application/json`
- Request: 기존 `PostCreateRequest`
- Success: `201 Created`, `{ "id": 1, "memberId": 10, "title": "...", "content": "..." }`
- Errors: `400` validation, `401` token 없음·변조·만료
- Note: memberId는 요청받지 않고 JWT `sub`에서 결정

### PUT `/api/v1/posts/{id}` / DELETE `/api/v1/posts/{id}`
- Headers: `Authorization: Bearer <JWT>`
- Success: 기존 계약 유지
- Errors: `401` 미인증, `403` 작성자 불일치, `404` 게시물 없음

### GET `/api/v1/posts` / GET `/api/v1/posts/{id}`
- Headers: 없음
- Success: 기존 응답에 `memberId` 추가
- Note: 공개 조회 유지

### POST `/api/v1/posts/{postId}/comments`
- Headers: `Authorization: Bearer <JWT>`, `Content-Type: application/json`
- Request: `{ "content": "댓글 내용" }`
- Validation: 공백 불가, 1~1000자
- Success: `201 Created`, `{ "id": 1, "postId": 1, "memberId": 10, "content": "댓글 내용", "createdAt": "UTC instant" }`
- Errors: `400` validation, `401` 미인증, `404` 게시물 없음

### GET `/api/v1/posts/{postId}/comments`
- Headers: 없음
- Success: `200 OK`, 댓글 응답 배열을 `createdAt ASC, id ASC`로 반환
- Errors: `404` 게시물 없음

## Data Models

### Member (`members`)
| Field | Type | Constraints |
|-------|------|-------------|
| `id` | BIGSERIAL | PK |
| `login_id` | VARCHAR(50) | NOT NULL, UNIQUE, API의 `id`에 대응 |
| `password` | VARCHAR(255) | NOT NULL, 해시 문자열 |
| `created_at` | TIMESTAMPTZ | NOT NULL, UTC |
| `updated_at` | TIMESTAMPTZ | NOT NULL, UTC |

### Post (`posts` 변경)
| Field | Type | Constraints |
|-------|------|-------------|
| `member_id` | BIGINT | NOT NULL, FK → members.id |
| 기존 필드 | 기존 타입 | 기존 제약 유지 |

### Comment (`comments`)
| Field | Type | Constraints |
|-------|------|-------------|
| `id` | BIGSERIAL | PK |
| `post_id` | BIGINT | NOT NULL, FK → posts.id, ON DELETE CASCADE |
| `member_id` | BIGINT | NOT NULL, FK → members.id |
| `content` | VARCHAR(1000) | NOT NULL |
| `created_at` | TIMESTAMPTZ | NOT NULL, UTC |
| `updated_at` | TIMESTAMPTZ | NOT NULL, UTC |

### Notification (`notifications`)
| Field | Type | Constraints |
|-------|------|-------------|
| `id` | BIGSERIAL | PK |
| `recipient_member_id` | BIGINT | NOT NULL, FK → members.id |
| `actor_member_id` | BIGINT | NOT NULL, FK → members.id |
| `post_id` | BIGINT | NOT NULL, FK → posts.id, ON DELETE CASCADE |
| `comment_id` | BIGINT | NOT NULL, FK → comments.id, ON DELETE CASCADE |
| `content` | VARCHAR(1000) | NOT NULL, 댓글 내용 snapshot |
| `created_at` | TIMESTAMPTZ | NOT NULL, UTC |
| unique | - | `(recipient_member_id, comment_id)` |

## Approval Record
- User approval source: 2026-07-17 요청 “로그인(회원가입) 단일 로직, 댓글 작성, 댓글 목록, 알림 생성 (service만) 이렇게 구현하는 방향으로 가자” 및 계획 파일 저장 요청
- Approved scope: 계획 생성과 위 기능 방향
- Execution approval: 2026-07-17 사용자 요청 “구현해줘”로 승인됨
- Out-of-scope items: Scope의 Out of Scope 항목 전체

## Role Routing
| Todo | Owner | Dependencies | Parallelizable | Context allowed | Context forbidden |
|------|-------|--------------|----------------|-----------------|-------------------|
| 1. 테스트 계약 확정 | product-decision | none | no | 본 계획과 사용자 답변 | 구현 변경 |
| 2. 스키마·보안 기반 | backend | 1 | no | build/config/schema/auth 계약 | frontend, Kafka 구현 |
| 3. 로그인 겸 회원가입 | backend | 2 | no | member/auth/security 파일 | comment/notification 내부 |
| 4. Post 회원 관계 | backend | 2, 3 | no | post/member/auth 계약 | 알림 구현 |
| 5. 댓글 API | backend | 3, 4 | no | comment/post/member 계약 | Kafka 구현 |
| 6. 알림 생성 Service | backend | 3, 4, 5 | no | notification/comment/member 계약 | 알림 API |
| 7. Swagger·통합 정리 | backend | 3, 4, 5, 6 | no | API DTO와 에러 계약 | frontend |
| 8. 최종 검증 | qa | 7 | no | 승인 계획, diff, 테스트 출력 | 기능 구현 |

## Dependency Tiers
- **Tier 0**: Test Case Decision과 실행 승인 확정
- **Tier 1**: Gradle 의존성, DB 스키마, JWT·에러 기반 구성
- **Tier 2**: 회원 Repository와 로그인 겸 자동 회원가입
- **Tier 3**: Post 회원 연관 및 작성자 권한
- **Tier 4**: 댓글 작성·목록 API
- **Tier 5**: 알림 생성 Service와 멱등성
- **Tier 6**: Swagger 문서 통합
- **Tier 7**: 기능·전체 QA

공유 파일인 `schema.sql`, 보안 설정, Testcontainers DB 상태와 Post 계약 의존성이 있으므로 구현은 직렬화한다.

## Test Case Plan
| # | Task/Todo | Target behavior | Scenario | Design method | Input | Expected | Priority |
|---|-----------|-----------------|----------|---------------|-------|----------|----------|
| 1 | 로그인 | 신규 회원 자동 생성 | 유효 신규 로그인 ID | 등가분할(유효) | 신규 ID, 8자 이상 비밀번호 | 회원 한 건 생성, 해시 저장, JWT, `isNewMember=true` | 필수 |
| 2 | 로그인 | 기존 회원 인증 | 유효 기존 계정 | 상태전환 | 가입된 ID, 일치 비밀번호 | 회원 수 유지, JWT, `isNewMember=false` | 필수 |
| 3 | 로그인 | 잘못된 비밀번호 거부 | 인증 실패 | 등가분할(무효) | 기존 ID, 불일치 비밀번호 | `401`, 신규 회원 없음 | 필수 |
| 4 | 로그인 | 입력 경계 검증 | ID·비밀번호 경계 위반 | 경계값 | 공백/51자 ID 또는 7자 비밀번호 | `400` | 필수 |
| 5 | JWT | 보호 API 인증 | token 없음·변조·만료 | 에러추측 | 잘못된 Authorization | `401` | 필수 |
| 6 | Post | 작성자 연결 | 인증된 생성 | 상태전환 | JWT + 유효 게시물 | `posts.member_id`가 JWT `sub`와 일치 | 필수 |
| 7 | Post | 소유권 강제 | 타인 수정·삭제 | 상태전환(불가능) | 다른 회원 JWT | `403`, DB 변경 없음 | 필수 |
| 8 | Comment | 정상 댓글 작성 | 유효 댓글 | 등가분할(유효) | JWT, 존재 postId, content | 작성자·게시물에 연결, `201` | 필수 |
| 9 | Comment | 없는 게시물 거부 | 잘못된 FK 대상 | 에러추측 | 존재하지 않는 postId | `404`, 댓글 없음 | 필수 |
| 10 | Comment | 내용 경계 검증 | 공백·1001자 | 경계값 | invalid content | `400` | 필수 |
| 11 | Comment | 목록 정렬 | 동일 게시물 여러 댓글 | CORRECT(ordering) | 생성 시각/ID가 다른 댓글 | 오래된 순서, 다른 게시물 댓글 제외 | 필수 |
| 12 | Notification | 정상 알림 생성 | 타인 댓글 | 등가분할(유효) | recipient != actor, 유효 post/comment | 알림 한 건 생성 | 필수 |
| 13 | Notification | 중복 생성 방지 | 재처리 | 에러추측(중복) | 동일 recipient/comment 2회 | 알림 한 건 유지 | 필수 |
| 14 | Notification | 자기 알림 생략 | 자기 게시물 댓글 | 등가분할 | recipient == actor | 알림 생성 없음 | 필수 |
| 15 | Swagger | 공개 문서와 JWT scheme | OpenAPI 조회 | Right-BICEP | `/v3/api-docs` | auth/comment 경로와 Bearer scheme 존재 | 필수 |
| 16 | Regression | 기존 게시물 조회 | 공개 GET | 회귀 | 인증 없는 목록·단건 요청 | `200`, 기존 필드와 memberId 반환 | 필수 |

## QA Matrix
| Gate | Command/artifact | Required | Expected evidence | Owner |
|------|------------------|----------|-------------------|-------|
| Narrow auth | `./gradlew test --tests '*AuthControllerIntegrationTests'` | yes | 신규/기존/실패/JWT 케이스 exit 0 | backend → qa |
| Narrow post | `./gradlew test --tests '*PostControllerIntegrationTests'` | yes | 회원 연관·권한·기존 CRUD exit 0 | backend → qa |
| Narrow comment | `./gradlew test --tests '*CommentControllerIntegrationTests'` | yes | 작성·validation·404·정렬 exit 0 | backend → qa |
| Narrow notification | `./gradlew test --tests '*NotificationServiceIntegrationTests'` | yes | 생성·중복·자기 알림 exit 0 | backend → qa |
| Feature suite | `./gradlew test --tests 'com.roky.kafkaaz.auth.*' --tests 'com.roky.kafkaaz.comment.*' --tests 'com.roky.kafkaaz.notification.*' --tests 'com.roky.kafkaaz.post.*'` | yes | 기능 통합 exit 0 | qa |
| Full regression | `./gradlew test` | yes | 전체 테스트 exit 0 | qa |
| Build | `./gradlew build` | yes | 컴파일·테스트·패키징 exit 0 | qa |
| Diff review | `git diff --check` 및 `git diff --stat` | yes | whitespace 오류 없음, 범위 외 변경 없음 | qa |

## Flutter / Mobile Matrix
| Item | Value |
|------|-------|
| Flutter role required | no |
| Target platform(s) | 해당 없음 |
| State management/routing convention | 해당 없음 |
| Generated code command | 해당 없음 |
| Maestro required | no |
| App id / bundle id discovery | 해당 없음 |
| Device/cloud target | 해당 없음 |
| Reset/fixture strategy | Testcontainers PostgreSQL 초기화 |
| Required mobile artifacts | 없음 |

## External Research Log
| Question | Skill used | Source-backed conclusion | Plan impact |
|----------|------------|--------------------------|-------------|
| WebFlux JWT 검증 방식 | technical-search-skill | Spring Security Reactive Resource Server가 Bearer JWT 서명과 `exp`/`nbf`를 검증하고 기본 principal name을 `sub`에 매핑 | SecurityWebFilterChain과 ReactiveJwtDecoder 사용 |
| 비밀번호 저장 방식 | technical-search-skill | Spring Security PasswordEncoder는 비밀번호를 one-way adaptive hash로 저장·비교하도록 제공 | PasswordEncoder bean과 해시 컬럼 사용 |
| Swagger WebFlux 모듈 | technical-search-skill | 기존 springdoc WebFlux UI starter가 Spring Security 프로젝트의 OpenAPI/Swagger UI 노출을 지원 | 기존 의존성을 유지하고 Bearer SecurityScheme만 추가 |

## Cross-boundary Contracts
| Contract | Producer | Consumer | Success path | Guard/error path |
|----------|----------|----------|--------------|------------------|
| JWT `sub=memberId` | AuthService | SecurityConfig, PostController, CommentController | Long 회원 ID로 작성자 결정 | 파싱 불가·서명/만료 실패 `401` |
| Post ownership | PostService | Post Repository/API | JWT 회원 ID를 `posts.member_id`에 저장 | 다른 회원 수정·삭제 `403` |
| Comment response | CommentService | HTTP client, 미래 event producer | commentId/postId/memberId/content/createdAt 반환 | 없는 post `404`, invalid content `400` |
| Notification create | 미래 Kafka Consumer | NotificationService | recipient/actor/post/comment/content로 한 건 저장 | 자기 알림 skip, 중복은 기존 한 건 유지 |

## Halt Conditions
- **Scope drift**: refresh token, 댓글 수정·삭제, 알림 API, Kafka Consumer가 필요해지면 중단하고 계획을 재승인한다.
- **Product decision**: 자동 가입 조건, 댓글 정렬, 자기 알림 정책, 알림 직접 호출 여부가 변경되면 중단한다.
- **Security/data decision**: POC 고정 JWT secret을 운영 용도로 사용하거나 기존 DB 데이터의 `posts.member_id` backfill 정책이 필요하면 중단한다.
- **Missing command/env**: Java 25, Docker 또는 PostgreSQL Testcontainers를 실행할 수 없어 필수 테스트 근거를 만들 수 없으면 blocker로 보고한다.
- **Shared-state conflict**: 사용자 변경과 `schema.sql`, Gradle, Post 파일이 겹치면 덮어쓰지 않고 중단한다.

## Implementation Todos

### Todo 1: 테스트 결정과 실행 승인 확정
- **Priority**: 0
- **Dependencies**: none
- **Goal**: 테스트 계약과 구현 범위를 사용자 승인 상태로 만든다.
- **Work**:
  - Test Case Decision의 각 행을 `Required` 또는 사유가 있는 `Skip`으로 확정한다.
  - 자동 가입, 댓글 공개 목록, 알림 Service-only 경계를 승인 기록에 반영한다.
- **Convention Notes**: 불확실한 기대 동작을 임의로 구현하지 않는다.
- **Verification**: 모든 Test Case Decision에서 `Required / Skip` 문자열이 사라졌는지 확인한다.
- **Exit Criteria**: 실행 승인과 테스트 결정이 모두 기록됨.
- **Status**: completed

### Todo 2: DB와 보안 기반 추가
- **Priority**: 1
- **Dependencies**: Todo 1
- **Goal**: 회원·연관 데이터와 JWT 인증을 구현할 기반을 만든다.
- **Work**:
  - `build.gradle.kts`에 Spring Security와 OAuth2 Resource Server/Jose 지원을 추가한다.
  - `schema.sql`에 members를 먼저 만들고 posts.member_id, comments, notifications와 FK/unique/index를 정의한다.
  - `JwtProperties`, JWT encoder/decoder, `SecurityWebFilterChain`, 공통 ErrorResponse를 추가한다.
  - auth, Swagger 경로와 공개 GET은 permit, Post mutation과 Comment POST는 authenticated로 설정한다.
- **Convention Notes**: secret은 POC 요청에 따라 application.yaml에만 고정하고 로그에 출력하지 않는다.
- **Verification**: `./gradlew compileKotlin`; 보안 없는 기존 context 테스트의 예상 변경을 확인한다.
- **Exit Criteria**: 앱 context가 JWT 설정과 신규 스키마로 기동 가능함.
- **Status**: completed

### Todo 3: 로그인 겸 자동 회원가입 구현
- **Priority**: 2
- **Dependencies**: Todo 2
- **Goal**: 단일 API로 신규 가입과 기존 회원 로그인을 처리한다.
- **Work**:
  - Member, MemberRepository, LoginRequest/Response, MemberMapper를 추가한다.
  - `AuthService.login`에서 로그인 ID 조회 후 부재 시 hash 저장, 존재 시 password match를 수행한다.
  - JwtTokenService가 memberId를 `sub`에 넣고 issuer/iat/exp가 있는 access token을 발급한다.
  - AuthController와 AuthControllerDocs를 추가한다.
  - 승인된 auth 테스트를 production code 전에 작성해 RED를 확인한 뒤 GREEN으로 만든다.
- **Convention Notes**: API 필드는 `id`, 도메인·DB 필드는 `loginId`/`login_id`로 구분한다. 기존 로그인 ID의 비밀번호 불일치는 `401`로 처리한다.
- **Verification**:
  - `RED (environment-blocked): ./gradlew test --tests '*AuthControllerIntegrationTests' → Caused by: java.lang.IllegalStateException at DockerClientProviderStrategy.java:274`
  - `GREEN: ./gradlew test --tests '*AuthControllerIntegrationTests' → BUILD SUCCESSFUL in 11s`
- **Exit Criteria**: 신규·기존·실패·JWT 보호 계약이 모두 통과함.
- **Status**: completed

### Todo 4: Post 회원 연관과 소유권 구현
- **Priority**: 3
- **Dependencies**: Todo 3
- **Goal**: 모든 신규 게시물을 JWT 회원과 연결하고 mutation 권한을 강제한다.
- **Work**:
  - Post와 Repository 필드에 memberId를 추가한다.
  - Post 생성은 JWT `sub`를 전달받고 update/delete는 저장된 memberId와 비교한다.
  - PostResponse에 memberId를 노출하고 PostControllerDocs로 Swagger 명세를 분리한다.
  - Post 통합 테스트 fixture를 회원 로그인/JWT 기반으로 갱신하고 타인 mutation `403`을 추가한다.
- **Convention Notes**: request body에서 memberId를 받지 않는다.
- **Verification**:
  - `RED (expected — unimplemented): ./gradlew test --tests '*PostControllerIntegrationTests' → PostControllerIntegrationTests > creates reads updates and deletes a post() FAILED; IntegrityConstraintViolationException`
  - `GREEN: ./gradlew test --tests '*PostControllerIntegrationTests' → BUILD SUCCESSFUL in 12s`
- **Exit Criteria**: 작성자 연결, 공개 조회, 소유권, 기존 CRUD가 통과함.
- **Status**: completed

### Todo 5: 댓글 작성·목록 API 구현
- **Priority**: 4
- **Dependencies**: Todo 4
- **Goal**: 인증 회원이 댓글을 작성하고 누구나 게시물 댓글을 조회하게 한다.
- **Work**:
  - Comment, CommentCreateRequest, CommentResponse/Mapper를 추가한다.
  - CommentRepository에 create와 `findAllByPostIdOrderByCreatedAtAscIdAsc`를 구현한다.
  - CommentService에서 게시물 존재 여부를 확인하고 작성자를 JWT memberId로 저장한다.
  - CommentController/Docs에 POST와 GET 계약을 구현한다.
  - validation, 404, 정렬·격리 통합 테스트를 RED→GREEN으로 수행한다.
- **Convention Notes**: 댓글 존재 여부 확인과 조회 쿼리 책임을 분리하고 N+1 조회를 만들지 않는다.
- **Verification**:
  - `RED (expected — unimplemented): ./gradlew test --tests '*CommentControllerIntegrationTests' → 3 tests completed, 3 failed`
  - `GREEN: ./gradlew test --tests '*CommentControllerIntegrationTests' → BUILD SUCCESSFUL in 17s`
- **Exit Criteria**: 작성·검증·404·정렬·게시물별 격리 케이스 통과.
- **Status**: completed

### Todo 6: 알림 생성 Service 구현
- **Priority**: 5
- **Dependencies**: Todo 5
- **Goal**: 미래 Consumer가 댓글 기반 알림을 멱등하게 생성할 수 있게 한다.
- **Work**:
  - Notification, NotificationRepository, NotificationService를 추가한다.
  - `create(recipientMemberId, actorMemberId, postId, commentId, content)` 계약을 구현한다.
  - recipient와 actor가 같으면 저장 없이 완료한다.
  - `(recipient_member_id, comment_id)` 충돌은 오류 대신 기존 한 건 유지로 처리한다.
  - 정상·중복·자기 알림 테스트를 RED→GREEN으로 수행한다.
- **Convention Notes**: Controller를 만들지 않고 Service public contract만 제공한다.
- **Verification**:
  - `RED (expected — unimplemented): ./gradlew test --tests '*NotificationServiceIntegrationTests' → Unresolved reference 'NotificationService'`
  - `GREEN: ./gradlew test --tests '*NotificationServiceIntegrationTests' → BUILD SUCCESSFUL in 15s`
- **Exit Criteria**: 생성, 멱등성, 자기 알림 skip이 DB 상태로 검증됨.
- **Status**: completed

### Todo 7: Swagger와 API 에러 계약 완성
- **Priority**: 6
- **Dependencies**: Todo 3, Todo 4, Todo 5, Todo 6
- **Goal**: 실제 인증·댓글 API 계약과 에러를 OpenAPI에 노출한다.
- **Work**:
  - OpenApiConfig에 HTTP bearer JWT SecurityScheme을 추가한다.
  - Auth/Post/Comment Docs interface에 한국어 summary, description, 요청/응답 schema, 400/401/403/404를 명시한다.
  - Swagger UI와 OpenAPI 경로가 인증 없이 접근되도록 보안 설정을 검증한다.
  - OpenAPI JSON에서 auth/comment path와 security scheme을 assertion한다.
- **Convention Notes**: 알림 Service는 API가 아니므로 Swagger에 노출하지 않는다.
- **Verification**: 관련 Swagger integration test와 `./gradlew test --tests '*IntegrationTests'`.
- **Exit Criteria**: 문서 경로·응답 schema·Bearer scheme이 실제 API와 일치함.
- **Status**: completed

### Todo 8: 전체 QA와 범위 검증
- **Priority**: 7
- **Dependencies**: Todo 7
- **Goal**: 기능 계약, 회귀, 보안, 코드 품질을 명령 근거로 검증한다.
- **Work**:
  - QA Matrix의 narrow → feature → full → build 순서로 실행한다.
  - prompt-separated self-review로 인증 우회, IDOR, 비밀번호·secret 로그 노출, 알림 중복 race, 범위 외 기능을 점검한다.
  - 실패 시 backend/cross-boundary/product-decision으로 분류하고 최소 수정 후 같은 명령을 재실행한다.
- **Convention Notes**: 테스트 삭제·비활성화로 RED를 회피하지 않는다.
- **Verification**:
  - `./gradlew test --rerun-tasks` → `BUILD SUCCESSFUL in 19s`, 16 tests, failures 0, errors 0
  - `./gradlew build` → `BUILD SUCCESSFUL in 1s`
  - `git diff HEAD --check` → exit 0
  - prompt-separated self-review → `PASS`, blocker 없음
- **Exit Criteria**: QA verdict `PASS`, blocker 없음, 재현 가능한 명령 결과 기록.
- **Status**: completed

## Verification Strategy
- 승인된 테스트는 production code 전에 각 narrow 명령으로 RED를 확인하고 같은 명령으로 GREEN을 기록한다.
- Testcontainers PostgreSQL을 실제 저장 경계로 사용하고 내부 Repository/Service를 과도하게 mock하지 않는다.
- `./gradlew test`로 기존 application/Post 회귀와 신규 auth/comment/notification 계약을 함께 검증한다.
- `./gradlew build`로 최종 컴파일·테스트·패키징을 검증한다.
- 단일 에이전트 환경의 QA는 **prompt-separated self-review**로 명시하고 기계적 명령 결과를 우선 근거로 사용한다.

## Progress Tracking
- Total Todos: 8
- Completed: 8
- Status: Execution complete

## Change Log
- 2026-07-17: Plan created for single login/auto-signup, member-owned posts, comment create/list, and notification creation service
- 2026-07-17: Changed auto-signup credentials from email/password to ID/password and removed `expiresIn` from login response
- 2026-07-17: Todo 1 completed — all test decisions marked Required and execution approved
- 2026-07-17: Todo 2 completed — schema, JWT security, shared error foundation compiled successfully
- 2026-07-17: Todo 3 completed — auth integration GREEN; initial RED was environment-blocked until Docker Desktop started
- 2026-07-17: Todo 4 completed — Post member relation RED (integrity constraint) → GREEN (BUILD SUCCESSFUL)
- 2026-07-17: Todo 5 completed — Comment API RED (3 failed) → GREEN (BUILD SUCCESSFUL)
- 2026-07-17: Todo 6 completed — Notification Service compile RED → GREEN (BUILD SUCCESSFUL)
- 2026-07-17: Todo 7 completed — Swagger/API feature suite BUILD SUCCESSFUL in 21s
- 2026-07-17: Todo 8 completed — prompt-separated QA PASS; 16 tests, full build, diff check all successful
- 2026-07-17: Execution complete — all 8 todos completed
