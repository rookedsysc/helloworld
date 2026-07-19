# Kafka AZ Frontend

Spring Boot 백엔드 API와 연동하는 Next.js App Router 기반 게시물 UI입니다. 화면과 기능은 Feature-Sliced Design(FSD) 레이어로 분리되어 있습니다.

## 실행

```bash
npm install
npm run dev
```

기본 주소는 `http://localhost:4193`입니다. 로컬 개발에서는 runtime `BACKEND_API_URL` 기본값인 `http://localhost:8080/api`로 요청을 프록시합니다.

루트의 Spring Boot 애플리케이션을 실행하면 `spring-boot-docker-compose`가 `compose.yaml`의 frontend도 함께 시작합니다. 컨테이너 runtime에서는 API proxy가 `http://host.docker.internal:8080/api`를 사용하며 UI는 `http://localhost:4193`에 노출됩니다.

다른 backend 포트를 사용할 때는 image rebuild 없이 `BACKEND_API_URL=http://host.docker.internal:8082/api docker compose up -d frontend`처럼 runtime 값을 덮어쓸 수 있습니다.

## 페이지

- `/login`: 로그인 및 최초 ID 자동 가입
- `/posts`: 게시물 목록, loading·empty·error·retry 상태
- `/posts/new`: 인증 사용자용 게시물 작성
- `/posts/{postId}`: 게시물 상세, 댓글, 소유자 수정·삭제
- `/`: `/posts`로 이동

## 구조

- `app/`: Next.js route entry와 `/backend-api/[...path]` Route Handler
- `src/_app/`: global style과 runtime backend proxy
- `src/_pages/`: 로그인·목록·작성·상세 화면 조합
- `src/widgets/`: 여러 기능을 조합하는 site header
- `src/features/`: 로그인, 게시물 작성·수정·삭제, 댓글 작성
- `src/entities/`: member session, post, comment의 API·model·UI
- `src/shared/`: HTTP client, route 상수, 공통 error UI

Next.js의 예약 폴더와 FSD의 App/Pages 레이어가 충돌하지 않도록 FSD 레이어 이름은 `_app`, `_pages`를 사용합니다. 상위 레이어는 하위 레이어만 import하고 각 slice는 `index.ts` public API를 노출합니다.

## 공통 UI

- `SiteHeader`: 목록·작성·로그인 navigation과 session logout
- `ErrorNotice`: backend 오류와 retry action
- form UI: label, keyboard 접근, submitting/error 상태를 공통 style로 제공

## API

- `POST /api/v1/auth/login`
- `GET|POST /api/v1/posts`
- `GET|PUT|DELETE /api/v1/posts/{id}`
- `GET|POST /api/v1/posts/{postId}/comments`

브라우저에는 backend 주소를 노출하지 않습니다. Next Route Handler가 `/backend-api/**` 요청을 runtime `BACKEND_API_URL`로 전달하며, 연결 실패는 `{ "status": 502, "message": "백엔드에 연결할 수 없습니다." }`로 반환합니다.
