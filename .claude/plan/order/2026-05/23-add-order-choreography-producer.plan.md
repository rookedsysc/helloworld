# Order Choreography Producer

## Business Goal
주문 서비스가 Saga choreography 흐름의 시작점으로 동작하도록 주문 요청 상태 변경 후 Kafka 이벤트를 발행하는 최소 구현을 추가한다.

## Scope
- **In Scope**: common 모듈 Kafka topic/key 상수 추가, order 모듈 `order-placed` 이벤트 DTO와 producer 추가, choreography 전용 주문 처리 service 추가, 기존 place order endpoint를 choreography service로 연결
- **Out of Scope**: product/point consumer 구현, 보상 이벤트 처리, DLQ/retry 정책 구현, Kafka topic 자동 생성 설정, 테스트 컨테이너 기반 통합 테스트

## Codebase Analysis Summary
`distributed-transaction/order` 모듈은 Kotlin/Spring Boot 기반이며 `application`, `entity`, `infrastructure/in`, `infrastructure/out` 계층을 사용한다. 기존 `/orders/place` endpoint는 `OrderCoordinator`를 호출해 product/point HTTP API를 순차 호출하는 orchestration 방식이다. 이번 작업은 기존 coordinator를 제거하지 않고 choreography 전용 `OrderChoreographyService`를 추가해 endpoint가 주문 상태를 `REQUESTED`로 변경한 뒤 transaction commit 이후 Kafka 이벤트만 발행하도록 전환한다.

### Relevant Files
| File | Role | Action |
|------|------|--------|
| `distributed-transaction/common/src/main/kotlin/com/rookedsysc/common/kafka/KafkaNames.kt` | Kafka topic/key 중앙 관리 | Create |
| `distributed-transaction/order/src/main/kotlin/com/rookedsysc/order/infrastructure/kafka/dto/OrderPlacedEvent.kt` | `order-placed` 이벤트 payload | Create |
| `distributed-transaction/order/src/main/kotlin/com/rookedsysc/order/infrastructure/kafka/OrderPlacedProducer.kt` | Kafka producer adapter | Create |
| `distributed-transaction/order/src/main/kotlin/com/rookedsysc/order/application/OrderChoreographyService.kt` | 주문 요청 및 afterCommit 이벤트 발행 | Create |
| `distributed-transaction/order/src/main/kotlin/com/rookedsysc/order/infrastructure/in/OrderController.kt` | place order endpoint 연결 | Modify |

### Conventions to Follow
| Convention | Source | Rule |
|-----------|--------|------|
| Kotlin/Spring 구조 | 기존 order 모듈 | application service와 infrastructure adapter를 분리한다 |
| 생성자 주입 | 기존 Spring 컴포넌트 | primary constructor injection을 사용한다 |
| 최소 변경 | CODE_PRINCIPLES.md | 기존 orchestration coordinator는 삭제하지 않는다 |
| Kafka 이름 중앙화 | 사용자 요청 | topic/group/partition key 관련 이름은 common 모듈에서 관리한다 |

## Architecture Decisions
| Decision | Choice | Rationale | Alternatives |
|----------|--------|-----------|--------------|
| 이벤트 발행 시점 | `TransactionSynchronization.afterCommit` | DB transaction commit 이후에만 외부 Kafka 이벤트를 발행한다 | transaction 내부 즉시 발행 |
| 신규 service 이름 | `OrderChoreographyService` | 기존 `OrderService`, `OrderCoordinator`와 책임을 구분한다 | 기존 coordinator 수정 |
| Kafka topic | `KafkaTopics.ORDER_PLACED` | topic literal을 producer에 하드코딩하지 않는다 | producer 내부 문자열 |
| Partition key | `KafkaPartitionKeys.orderId(orderId)` | 주문 단위 이벤트 순서를 유지하기 위한 key 생성을 공통화한다 | `event.orderId.toString()` 직접 호출 |

## API Contracts (if applicable)

### POST `/orders/place`
- Headers: 없음
- Request: `PlaceOrderRequest`
- Response: 없음
- Note: 기존 orchestration HTTP 호출 대신 주문 상태 변경 후 `order-placed` Kafka event를 발행한다.

## Data Models (if applicable)

### OrderPlacedEvent
| Field | Type | Constraints |
|-------|------|-------------|
| `orderId` | `Long` | Kafka partition key로도 사용 |
| `productInfos` | `List<ProductInfo>` | 주문 상품 목록 |
| `ProductInfo.productId` | `Long` | 상품 식별자 |
| `ProductInfo.quantity` | `Long` | 주문 수량 |

## Implementation Todos

### Todo 1: Common Kafka 이름 상수 추가
- **Priority**: 1
- **Dependencies**: none
- **Goal**: topic/partition key 관련 이름을 common 모듈에서 중앙 관리한다.
- **Work**:
  - `KafkaNames.kt`를 생성한다.
  - `KafkaTopics.ORDER_PLACED`를 추가한다.
  - `KafkaPartitionKeys.orderId(orderId: Long)` helper를 추가한다.
- **Convention Notes**: common package는 `com.rookedsysc.common.kafka`를 사용한다.
- **Verification**: `./gradlew :common:compileKotlin`
- **Exit Criteria**: common 모듈 컴파일이 성공한다.
- **Status**: completed

### Todo 2: Order placed Kafka DTO와 producer 추가
- **Priority**: 2
- **Dependencies**: Todo 1
- **Goal**: `order-placed` topic에 주문 이벤트를 발행하는 infrastructure adapter를 만든다.
- **Work**:
  - `OrderPlacedEvent.kt`를 생성한다.
  - `OrderPlacedProducer.kt`를 생성한다.
  - `KafkaTemplate<String, OrderPlacedEvent>`를 사용한다.
  - topic은 `KafkaTopics.ORDER_PLACED`, key는 `KafkaPartitionKeys.orderId(event.orderId)`를 사용한다.
- **Convention Notes**: 사용자 예시는 Java지만 프로젝트 컨벤션에 맞춰 Kotlin으로 작성한다.
- **Verification**: `./gradlew :order:compileKotlin`
- **Exit Criteria**: order 모듈에서 producer와 DTO가 컴파일된다.
- **Status**: completed

### Todo 3: Choreography 전용 주문 service 추가
- **Priority**: 3
- **Dependencies**: Todo 2
- **Goal**: 주문을 요청 상태로 변경하고 commit 이후 Kafka 이벤트를 발행한다.
- **Work**:
  - `OrderChoreographyService.kt`를 생성한다.
  - `PlaceOrderCommand`를 받아 order와 orderItems를 조회한다.
  - `order.request()` 후 `orderRepository.save(order)`를 수행한다.
  - `TransactionSynchronizationManager.registerSynchronization`과 `TransactionSynchronization.afterCommit`으로 `OrderPlacedProducer.send(...)`를 호출한다.
- **Convention Notes**: 비즈니스 흐름은 application layer에 두고 Kafka 발행은 infrastructure producer에 위임한다.
- **Verification**: `./gradlew :order:compileKotlin`
- **Exit Criteria**: transaction synchronization 코드가 컴파일되고 producer에 의존한다.
- **Status**: completed

### Todo 4: Place order endpoint를 choreography service로 연결
- **Priority**: 4
- **Dependencies**: Todo 3
- **Goal**: 기존 주문 처리 endpoint가 choreography 전용 service를 호출하도록 한다.
- **Work**:
  - `OrderController`의 `OrderCoordinator` dependency를 `OrderChoreographyService`로 변경한다.
  - `placeOrder`에서 `orderChoreographyService.placeOrder(...)`를 호출한다.
- **Convention Notes**: endpoint path와 request DTO는 변경하지 않는다.
- **Verification**: `./gradlew :order:compileKotlin`
- **Exit Criteria**: controller가 새 service를 참조하며 order 모듈 컴파일이 성공한다.
- **Status**: completed

## Verification Strategy
- `cd distributed-transaction && ./gradlew :common:compileKotlin :order:compileKotlin`
- 변경 파일 diff 검토
- 전체 `compileKotlin`은 기존 monolithic 모듈 오류가 있어 범위 검증 대상에서 제외한다.

## Progress Tracking
- Total Todos: 4
- Completed: 4
- Status: Execution complete

## Change Log
- 2026-05-23: Plan created
- 2026-05-23: Todo 1 completed - Common Kafka 이름 상수 추가
- 2026-05-23: Todo 2 completed - Order placed Kafka DTO와 producer 추가
- 2026-05-23: Todo 3 completed - Choreography 전용 주문 service 추가
- 2026-05-23: Todo 4 completed - Place order endpoint를 choreography service로 연결
- 2026-05-23: Execution complete - common/order compile 검증 완료
