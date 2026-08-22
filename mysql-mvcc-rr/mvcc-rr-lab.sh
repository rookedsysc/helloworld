#!/usr/bin/env bash
#
# MySQL InnoDB REPEATABLE READ 에서 consistent read 와 current read 가
# 각각 어느 버전을 보는지 두 세션을 실제로 인터리빙해 확인한다.
#
# 검증 대상
#   1. read view 는 BEGIN 이 아니라 첫 consistent read 시점에 만들어지는가
#   2. START TRANSACTION WITH CONSISTENT SNAPSHOT 은 시작 시점에 만드는가
#   3. FOR UPDATE 로 최신값을 본 뒤 다시 일반 SELECT 하면 스냅샷으로 돌아가는가
#   4. 내가 직접 UPDATE 한 행은 이후 일반 SELECT 에 보이는가
#
# 순서 보장: 출력 파싱 대신 DO SLEEP() 으로 두 세션의 시각을 벌린다.
# (mysql 클라이언트는 stdout 이 파일이면 full-buffered 라 마커 폴링이 안 통한다.
#  DO 는 SELECT 가 아니므로 InnoDB read view 를 앞당겨 만들지 않는다.)
#
set -euo pipefail

CT=mvcc-rr-lab
IMG=mysql:8.4.10
PW=localpw
DIR="$(cd "$(dirname "$0")" && pwd)"
OUT="$DIR/out"

rm -rf "$OUT"; mkdir -p "$OUT"
log() { printf '%s\n' "$*" | tee -a "$OUT/run.log"; }

cleanup() { docker rm -f "$CT" >/dev/null 2>&1 || true; }
trap cleanup EXIT

# ---------------------------------------------------------------- 컨테이너
log "== 컨테이너 기동 =="
docker rm -f "$CT" >/dev/null 2>&1 || true
docker run -d --name "$CT" \
  -e MYSQL_ROOT_PASSWORD="$PW" -e MYSQL_DATABASE=mvcc "$IMG" >/dev/null

# mysqladmin ping 은 엔트리포인트의 임시 부트스트랩 서버에도 성공한다.
# 그 시점엔 MYSQL_DATABASE 가 아직 없으므로 대상 DB 에 실제 쿼리가 통할 때까지 기다린다.
i=0
until docker exec "$CT" mysql -uroot -p"$PW" --database=mvcc -e "SELECT 1" >/dev/null 2>&1; do
  i=$((i+1)); [ "$i" -gt 120 ] && { log "FAIL: mysql 기동 타임아웃"; exit 1; }
  sleep 1
done
log "mysql ready (${i}s)"

sql() {   # stdin 의 SQL 을 새 커넥션에서 실행. 비밀번호 경고만 제거.
  docker exec -i "$CT" mysql -uroot -p"$PW" --database=mvcc --batch --raw \
    2> >(grep -v 'Using a password on the command line' >&2)
}

sql <<'SQL'
DROP TABLE IF EXISTS t1, t2, t3;
CREATE TABLE t1 (id INT PRIMARY KEY, v INT) ENGINE=InnoDB;
CREATE TABLE t2 (id INT PRIMARY KEY, v INT) ENGINE=InnoDB;
CREATE TABLE t3 (id INT PRIMARY KEY, v INT) ENGINE=InnoDB;
INSERT INTO t1 VALUES (1,1); INSERT INTO t2 VALUES (1,1); INSERT INTO t3 VALUES (1,1);
SQL

log ""
log "== 환경 =="
sql <<<"SELECT @@version AS version, @@transaction_isolation AS isolation;" | tee -a "$OUT/run.log"

# 결과 파일에서 라벨의 값을 꺼내 기대값과 대조한다.
PASS=0; FAIL=0
check() {  # check <라벨> <기대값> <파일>
  local label=$1 want=$2 file=$3 got
  got=$(awk -v l="$label" '$1==l {print $2}' "$file" | head -1)
  if [ "$got" = "$want" ]; then
    log "  PASS  $label = $got"; PASS=$((PASS+1))
  else
    log "  FAIL  $label = ${got:-<없음>}  (기대 $want)"; FAIL=$((FAIL+1))
  fi
}

# ------------------------------------------------------------- 시나리오 1
log ""
log "== 시나리오 1: read view 는 BEGIN 이 아니라 첫 consistent read 에서 생긴다 =="
log "   A: BEGIN → 2초 대기 → 첫 SELECT   /   B: 1초 뒤 9로 UPDATE 커밋"
sql > "$OUT/s1-A.txt" <<'SQL' &
BEGIN;
DO SLEEP(2);
SELECT 'A_first_read' AS label, v AS value FROM t2 WHERE id=1;
ROLLBACK;
SQL
PA=$!
sql > "$OUT/s1-B.txt" <<'SQL' &
DO SLEEP(1);
UPDATE t2 SET v=9 WHERE id=1;
SQL
wait $PA $!
# BEGIN 시점에 고정된다면 1, 첫 SELECT 시점이라면 9
check A_first_read 9 "$OUT/s1-A.txt"

# ------------------------------------------------------------- 시나리오 2
log ""
log "== 시나리오 2: WITH CONSISTENT SNAPSHOT 은 시작 시점에 고정한다 =="
log "   A: START ... WITH CONSISTENT SNAPSHOT → 2초 대기 → 첫 SELECT   /   B: 1초 뒤 9로 커밋"
sql > "$OUT/s2-A.txt" <<'SQL' &
START TRANSACTION WITH CONSISTENT SNAPSHOT;
DO SLEEP(2);
SELECT 'A_first_read' AS label, v AS value FROM t3 WHERE id=1;
ROLLBACK;
SQL
PA=$!
sql > "$OUT/s2-B.txt" <<'SQL' &
DO SLEEP(1);
UPDATE t3 SET v=9 WHERE id=1;
SQL
wait $PA $!
check A_first_read 1 "$OUT/s2-A.txt"

# ------------------------------------------------------------- 시나리오 3
log ""
log "== 시나리오 3: consistent read vs current read, 그리고 되돌아감 =="
log "   A: BEGIN → SELECT(스냅샷 생성) → 2초 대기 → 일반/FOR UPDATE/일반/UPDATE/일반"
log "   B: 1초 뒤 3으로 UPDATE 커밋"
sql > "$OUT/s3-A.txt" <<'SQL' &
BEGIN;
SELECT 'A1_snapshot_created' AS label, v AS value FROM t1 WHERE id=1;
DO SLEEP(2);
SELECT 'A2_plain'          AS label, v AS value FROM t1 WHERE id=1;
SELECT 'A3_for_update'     AS label, v AS value FROM t1 WHERE id=1 FOR UPDATE;
SELECT 'A4_plain_again'    AS label, v AS value FROM t1 WHERE id=1;
UPDATE t1 SET v = v + 1 WHERE id=1;
SELECT 'A5_after_my_update' AS label, v AS value FROM t1 WHERE id=1;
COMMIT;
SQL
PA=$!
sql > "$OUT/s3-B.txt" <<'SQL' &
DO SLEEP(1);
UPDATE t1 SET v=3 WHERE id=1;
SQL
wait $PA $!

check A1_snapshot_created 1 "$OUT/s3-A.txt"   # 스냅샷 생성 시점의 값
check A2_plain            1 "$OUT/s3-A.txt"   # 스냅샷 유지 (B의 3이 안 보임)
check A3_for_update       3 "$OUT/s3-A.txt"   # current read → 최신 커밋
check A4_plain_again      1 "$OUT/s3-A.txt"   # ★ 스냅샷으로 되돌아감 (3이 아님)
check A5_after_my_update  4 "$OUT/s3-A.txt"   # 내가 쓴 행이라 보임 (3+1)

log ""
log "== 최종 커밋값 =="
sql <<<"SELECT 't1' AS tbl, v FROM t1 UNION ALL SELECT 't2', v FROM t2 UNION ALL SELECT 't3', v FROM t3;" \
  | tee -a "$OUT/run.log"

log ""
log "== 결과: PASS=$PASS FAIL=$FAIL =="
[ "$FAIL" -eq 0 ] || exit 1
