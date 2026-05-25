# Distributed Transaction

## CLI 실행 방법

이 프로젝트의 Gradle Wrapper는 저장소 루트가 아니라 `distributed-transaction` 디렉터리 안에 있다.

저장소 루트에서 바로 `./gradlew bootRun`을 실행하면 아래처럼 실패한다.

```bash
zsh: no such file or directory: ./gradlew
```

먼저 `distributed-transaction` 디렉터리로 이동해서 실행한다.

```bash
cd distributed-transaction
```

## 인프라 실행

애플리케이션은 MySQL, Redis, Kafka를 사용한다. 로컬 Docker Compose로 필요한 인프라를 먼저 실행한다.

```bash
docker compose up -d dt-db dt-redis dt-kafka
```

모니터링 도구까지 함께 실행하려면 다음 명령을 사용한다.

```bash
docker compose up -d
```

## 모듈별 실행

각 애플리케이션은 별도 터미널에서 실행한다.

```bash
./gradlew :product:bootRun
./gradlew :order:bootRun
./gradlew :point:bootRun
./gradlew :monolithic:bootRun
```

`common`은 공유 모듈이라 보통 직접 실행하지 않고 `product`, `order`, `point`에서 의존해서 사용한다.

## 서비스 포트

| 모듈 | 포트 |
| --- | --- |
| `monolithic` | `9780` |
| `product` | `9785` |
| `order` | `9786` |
| `point` | `9787` |
