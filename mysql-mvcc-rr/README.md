# MySQL InnoDB REPEATABLE READ — consistent read vs current read

"REPEATABLE READ는 트랜잭션 시작 시점으로 MVCC 스냅샷이 고정된다"는 흔한 요약이
MySQL InnoDB에서 어디까지 맞고 어디서 새는지를 두 세션을 실제로 인터리빙해 확인한다.

```bash
./mvcc-rr-lab.sh          # Docker 필요. 기대값 검증 내장, FAIL 시 exit 1
```

`mysql:8.4.10` 컨테이너를 띄우고 세 시나리오를 돌린 뒤 컨테이너를 정리한다.
결과는 `out/`에 남는다(gitignore).

## 결과 (2026-08-22, mysql 8.4.10, REPEATABLE-READ)

### 시나리오 1 — read view는 `BEGIN`이 아니라 첫 consistent read에서 생긴다

```
A: BEGIN;  DO SLEEP(2);  SELECT v FROM t2;   -- → 9
B:         DO SLEEP(1);  UPDATE t2 SET v=9;  -- autocommit
```

`BEGIN` 시점에 고정된다면 `1`이 나와야 하지만 **`9`** 가 나온다.
A가 아무것도 읽지 않는 동안 B가 커밋한 값이 그대로 보인다.

### 시나리오 2 — `WITH CONSISTENT SNAPSHOT`은 시작 시점에 고정한다

```
A: START TRANSACTION WITH CONSISTENT SNAPSHOT;  DO SLEEP(2);  SELECT v FROM t3;  -- → 1
B:                                              DO SLEEP(1);  UPDATE t3 SET v=9;
```

같은 타이밍인데 **`1`**. "시작 시점 고정"은 이 문법을 명시할 때만 성립한다.

### 시나리오 3 — current read를 본 뒤 일반 SELECT는 스냅샷으로 되돌아간다

```
A: BEGIN;
   SELECT v FROM t1;              -- A1 → 1   스냅샷 생성
   DO SLEEP(2);                   --           B가 3으로 커밋
   SELECT v FROM t1;              -- A2 → 1   스냅샷 유지
   SELECT v FROM t1 FOR UPDATE;   -- A3 → 3   current read
   SELECT v FROM t1;              -- A4 → 1   ★ 되돌아감
   UPDATE t1 SET v = v + 1;       --           current read(3) 기준
   SELECT v FROM t1;              -- A5 → 4   내가 쓴 행이라 보임
   COMMIT;                        --           최종 커밋값 4
```

`A3 → A4`에서 같은 행을 읽는데 **3 → 1로 거꾸로 간다.**
`FOR UPDATE`가 최신값을 한 번 봤다고 트랜잭션 스냅샷이 갱신되지는 않는다.

`A5 = 4`는 `UPDATE`가 스냅샷의 `1`이 아니라 current read인 `3`을 기준으로
계산했다는 뜻이다.

## 정리

| 문장 | 보는 값 | 스냅샷 갱신 |
| --- | --- | --- |
| 일반 `SELECT` | 스냅샷 | 첫 회에 생성, 이후 유지 |
| `SELECT ... FOR UPDATE` / `FOR SHARE` | 최신 커밋 | 안 함 |
| `UPDATE` / `DELETE` / `INSERT ... SELECT` | 최신 커밋 | 안 함 (단 **그 행**은 이후 보임) |

락을 잡는 문장은 전부 current read다. 락은 실재하는 최신 행에 걸어야 의미가 있으니
과거 버전을 보면서 락을 잡을 수 없다.

일반 `SELECT`가 스냅샷으로 돌아가는 규칙의 유일한 예외는
**내 트랜잭션이 직접 `UPDATE`/`DELETE`한 행**이다. 락만 잡은 `FOR UPDATE`는 예외가 아니다.

PostgreSQL의 Repeatable Read는 DML도 트랜잭션 스냅샷에서 대상을 찾고 충돌 시
직렬화 실패를 던지므로, "RR = 스냅샷 고정"이라는 설명이 더 잘 맞는 쪽은 PostgreSQL이다.

## 구현 메모

순서 보장에 출력 파싱 대신 `DO SLEEP()`을 쓴다. 이유가 두 가지다.

- mysql 클라이언트는 stdout이 파일이면 full-buffered라, 세션을 FIFO로 열어 두고
  완료 마커를 폴링하는 방식이 동작하지 않는다.
- `DO`는 `SELECT`가 아니라 InnoDB 테이블을 건드리지 않으므로 read view를 앞당겨
  만들지 않는다. `SELECT SLEEP(...)`을 쓰면 그 자체가 consistent read가 될 소지가 있다.

기동 확인에 `mysqladmin ping`을 쓰면 안 된다. 엔트리포인트의 임시 부트스트랩 서버에도
성공해서 `MYSQL_DATABASE`가 만들어지기 전에 통과한다. 대상 DB에 실제 쿼리를 날려 확인한다.
