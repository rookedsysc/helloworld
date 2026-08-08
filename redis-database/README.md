# Redis logical database PoC

Redis의 `DB 0`, `DB 1`은 독립 서버가 아니라 하나의 Redis 인스턴스 안에 있는 키 네임스페이스입니다. 이 PoC는 같은 키 이름의 분리, DB 단위 명령, 데이터셋 교환과 서버 설정 공유를 직접 확인합니다.

## 가장 빠르게 확인하기

`uv`, Docker와 Docker Compose가 필요합니다. 검증 코드는 Python 표준 라이브러리만 사용하므로 별도 패키지를 설치하지 않습니다.

```bash
cd redis-database
uv run verify.py
```

`uv`는 `verify.py`의 인라인 메타데이터를 읽어 Python 3.10 이상을 선택합니다. Python 코드는 Redis 명령별 검증 함수를 위에서 아래로 호출하므로 README의 학습 순서와 동일하게 읽을 수 있습니다.

성공하면 다음과 같은 결과가 출력됩니다.

```text
Redis 컨테이너를 시작합니다.
PASS: 같은 키 이름을 DB 0에서 독립적으로 조회
PASS: 같은 키 이름을 DB 1에서 독립적으로 조회
PASS: FLUSHDB가 다른 DB의 키를 유지
PASS: FLUSHDB가 선택한 DB만 비움
PASS: SWAPDB가 DB 0의 데이터셋을 교환
PASS: SWAPDB가 DB 1의 데이터셋을 교환
PASS: DB 1에서 바꾼 eviction 정책을 DB 0도 공유
PASS: eviction 정책은 logical DB별 설정이 아님
Redis logical database PoC 검증을 완료했습니다.
```

`verify.py`는 이 PoC 전용 Redis 컨테이너에 `FLUSHALL`을 실행해 매번 같은 초기 상태에서 검증합니다. 외부 Redis 주소에는 연결하지 않습니다.

## 직접 따라 해보기

Redis를 시작합니다.

```bash
docker compose up -d --wait
```

### 같은 키 이름을 분리한다

`-n`은 연결할 logical database 번호를 선택하는 `redis-cli` 옵션입니다. 내부적으로는 연결에 `SELECT` 명령을 적용합니다.

```bash
docker compose exec redis redis-cli -n 0 SET user:1 db-0-user
docker compose exec redis redis-cli -n 1 SET user:1 db-1-user

docker compose exec redis redis-cli -n 0 GET user:1
docker compose exec redis redis-cli -n 1 GET user:1
```

각 DB에서 같은 `user:1`을 사용했지만 결과는 각각 `db-0-user`, `db-1-user`입니다. 이것이 logical database의 핵심 역할인 네임스페이스 분리입니다.

새 연결은 항상 DB 0에서 시작하고, 선택한 DB 번호는 연결 상태에 속합니다. 연결이 끊겼다가 다시 만들어지면 클라이언트가 DB를 다시 선택해야 합니다.

> Redis 공식 문서: [`SELECT`](https://redis.io/docs/latest/commands/select/)

### 선택한 DB만 조회하거나 비운다

`DBSIZE`, `SCAN`, `RANDOMKEY`, `FLUSHDB` 같은 명령은 현재 연결이 선택한 DB를 기준으로 동작합니다.

```bash
docker compose exec redis redis-cli -n 0 DBSIZE
docker compose exec redis redis-cli -n 1 DBSIZE

docker compose exec redis redis-cli -n 1 FLUSHDB
docker compose exec redis redis-cli -n 0 GET user:1
docker compose exec redis redis-cli -n 1 GET user:1
```

DB 1을 비워도 DB 0의 `user:1`은 남아 있습니다. 따라서 같은 애플리케이션 안에서 재생성 가능한 키 묶음을 통째로 비우는 용도로 사용할 수 있습니다.

### 두 DB의 데이터셋을 교환한다

`SWAPDB`는 두 logical database의 데이터셋을 교환합니다.

```bash
docker compose exec redis redis-cli -n 0 SET active-dataset blue
docker compose exec redis redis-cli -n 1 SET active-dataset green
docker compose exec redis redis-cli SWAPDB 0 1

docker compose exec redis redis-cli -n 0 GET active-dataset
docker compose exec redis redis-cli -n 1 GET active-dataset
```

결과는 DB 0이 `green`, DB 1이 `blue`입니다. 새 데이터셋을 다른 DB에서 준비한 뒤 교환하는 실험에 활용할 수 있습니다.

> Redis 공식 문서: [`SWAPDB`](https://redis.io/docs/latest/commands/swapdb/)

### 메모리와 eviction 정책은 공유한다

다음 명령은 DB 1 연결에서 eviction 정책을 바꾼 뒤 DB 0과 DB 1에서 각각 조회합니다.

```bash
docker compose exec redis redis-cli -n 1 CONFIG SET maxmemory-policy allkeys-lru
docker compose exec redis redis-cli -n 0 CONFIG GET maxmemory-policy
docker compose exec redis redis-cli -n 1 CONFIG GET maxmemory-policy
```

두 DB 모두 `allkeys-lru`를 반환합니다. `maxmemory`와 `maxmemory-policy`는 logical database 설정이 아니라 Redis 인스턴스 설정이기 때문입니다. 실험을 마친 뒤 기본 정책으로 되돌립니다.

```bash
docker compose exec redis redis-cli CONFIG SET maxmemory-policy noeviction
```

> Redis 공식 문서: [Key eviction](https://redis.io/docs/latest/develop/reference/eviction/)

## 무엇이 분리되고 무엇이 공유되는가

| 항목 | DB별 분리 여부 | 설명 |
| --- | --- | --- |
| 키 이름 공간 | 분리 | DB마다 같은 이름의 키를 저장할 수 있습니다. |
| `DBSIZE`, `SCAN`, `FLUSHDB` | 분리 | 현재 선택한 DB를 대상으로 실행됩니다. |
| 메모리 한도 | 공유 | `maxmemory`는 인스턴스 전체에 적용됩니다. |
| eviction 정책 | 공유 | `maxmemory-policy`는 인스턴스당 하나입니다. |
| CPU와 장애 영향 | 공유 | 하나의 Redis 프로세스를 사용합니다. |
| RDB/AOF와 복제 | 공유 | 모든 logical DB가 함께 저장되고 복제됩니다. |
| Pub/Sub | 공유 | 채널은 logical DB 네임스페이스에 속하지 않습니다. |

따라서 logical database는 같은 애플리케이션 안에서 키 묶음을 편리하게 관리할 때만 적합합니다. 서로 무관한 애플리케이션, 메모리 quota, 서로 다른 eviction 정책 또는 장애 격리가 필요하면 Redis 인스턴스를 분리해야 합니다.

Redis Cluster는 DB 0만 지원합니다. Cluster 전환 가능성이 있다면 logical database 대신 `cache:`, `lock:`, `session:` 같은 key prefix를 사용하는 편이 이식성이 좋습니다.

> Redis 공식 문서: [`SELECT`](https://redis.io/docs/latest/commands/select/), [Redis Pub/Sub](https://redis.io/docs/latest/develop/pubsub/)

Redis Software나 Redis Cloud에서 독립 리소스를 뜻하는 “database”는 이 PoC의 Redis Open Source logical database와 다른 개념입니다.

## 종료하기

이 PoC는 볼륨을 만들지 않습니다. 다음 명령으로 컨테이너와 네트워크를 제거할 수 있습니다.

```bash
docker compose down
```
