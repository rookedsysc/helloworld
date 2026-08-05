# opensearch-ngram-poc

한국어 소설처럼 **긴 본문**에 **1글자 검색**을 걸 때, MySQL FULLTEXT `ngram_token_size=1`과 OpenSearch 1-gram 중 무엇이 견디는지 실측하는 PoC.

> **현재 상태: 코드 완성, 측정 미실행.** 머신 자원 부족으로 적재·부하테스트 단계를 중단했다. 사유와 재개 방법은 [측정이 중단된 이유](#측정이-중단된-이유)를 참고한다.

---

## 무엇을 비교하는가

| | MySQL 8.4 | OpenSearch 2.19.1 |
| --- | --- | --- |
| 토큰화 | FULLTEXT `WITH PARSER ngram`, `ngram_token_size=1` | `ngram` tokenizer, `min_gram=max_gram=1` |
| 질의 | `MATCH(content) AGAINST (? IN BOOLEAN MODE)` | `{ "match": { "content": "다" } }` |
| 반환 | `LIMIT 20` | `size: 20` |

두 엔진의 **토큰 단위를 일치**시키는 것이 비교의 전제다. MySQL ngram 파서는 공백을 제거하므로 OpenSearch도 `token_chars: ["letter", "digit"]`로 공백·문장부호를 배제해 맞췄다. `max_gram - min_gram = 0`이라 `index.max_ngram_diff`(기본 1)는 건드리지 않는다.

정합이 실제로 맞는지는 부하테스트 전에 `npm run parity`로 검증하며, 매칭 문서 수 오차가 1%를 넘으면 벤치마크를 진행하지 않는다.

---

## 실측으로 확인한 사실

### 1글자 검색은 선택도가 사실상 0이다

프로젝트의 실제 소설 산문 895,466자에서 음절 분포를 뽑은 결과, **고유 음절은 1,482종**이고 분포는 강한 Zipf 형태다.

| 음절 | 코퍼스 내 비중 | 7,500자 문서당 기대 출현 |
| --- | --- | --- |
| `다` | 5.31% | 398회 |
| `이` | 4.47% | 335회 |
| `는` | 3.89% | 291회 |
| `었` | 2.45% | 184회 |
| `지` | 2.01% | 151회 |

문서당 7,500자를 뽑으면 **빈도 0.1% 이상인 음절은 99.9%의 문서에 등장**한다. 상위 약 300음절은 포함률이 100%로 수렴한다. 즉 자연 빈도만으로는 "10%의 문서에만 있는 글자" 같은 중간 선택도를 만들 수 없다.

그래서 계층을 이렇게 나눈다.

| 계층 | 질의 문자 | 목표 포함률 | 만드는 방법 |
| --- | --- | --- | --- |
| `COMMON` | 다 이 는 었 지 | 100% | **자연 빈도 그대로.** 주입하면 posting list가 실제보다 작아져 최악 조건을 과소평가한다 |
| `MEDIUM` | 촉 뗀 찜 솜 믄 | 10% | 본문 풀에서 배제 후 결정적 주입 |
| `RARE` | 냬 킼 휙 빽 쩝 | 0.1% | 본문 풀에서 배제 후 결정적 주입 |

이 문자들은 본문 생성기와 동등성 게이트가 읽는 `data/tier-query-characters.json`, 그리고 k6 스크립트 `k6/single-char-search.js`의 `TIER_QUERY_CHARACTERS` 상수 두 곳에 적혀 있다. k6는 컨테이너에 stdin으로 스크립트만 넘겨 실행하므로 JSON을 읽을 수 없어 값을 직접 들고 있으니, 문자를 바꿀 때는 두 곳을 함께 고친다.

마커는 코퍼스 희귀 음절(출현 2회 이하)에서 골라 풀에서 빼도 분포가 거의 왜곡되지 않게 했다. 계층당 5종을 두는 이유는 캐시 오염 방지다 — 같은 글자만 수천 번 쏘면 버퍼풀과 Lucene 캐시가 완전히 데워져 실제보다 빠른 수치가 나온다.

### 데이터는 왜 합성인가

프로젝트 DB 덤프(231MB, 387 테이블)를 조사했으나 **덤프 전체 한글이 1,530,605자**로, 필요량(10만건 × 7,500자 = 7.5억 자)의 **0.20%**에 불과했다. 실제 소설 산문은 895,466자로 문서 204건 분량이다. 따라서 덤프는 **음절 빈도 프로파일 추출에만** 사용하고 본문은 그 분포를 따라 합성한다.

집계 통계(`data/korean-syllable-frequency.json`, 20KB)만 저장하며 원문 문장이나 사용자 식별자는 저장하지 않는다. 실제 문장을 문서에 채우지 않는 이유는 895k자를 7.5억 자까지 채우려면 838회 반복해야 하는데, 1-gram 색인은 문서별 문자 다중집합만 보므로 실제 문장 연결과 빈도 샘플링의 색인 특성이 사실상 같기 때문이다.

---

## 실행 절차

### 0. 사전 조건

- Docker, Node.js 20+ (OpenSearch 힙 메모리 잠금은 비특권 컨테이너에서 기동이 실패하므로 꺼 두었다. 호스트가 `memlock` 무제한을 허용하면 켜는 편이 재현성에 낫다)
- **가용 RAM 5GB 이상** (MySQL buffer pool 2G + OpenSearch heap 2G + 앱 0.5G)
- 디스크 30GB 이상

```bash
free -h        # available 5.0Gi 이상인지 확인
nproc          # 4코어 이상 권장
```

자원이 부족하거나 **동작만 확인하고 싶다면** 아래 [최소 부하 동작 검증](#최소-부하-동작-검증)을 먼저 보라. 데이터와 부하를 줄여 1GB 미만으로 전체 파이프라인을 돌릴 수 있다.

### 1. 의존성

```bash
npm install
npm run build   # exit 0
npm test        # 생성기 계약 4종
```

### 2. 컨테이너 기동

```bash
docker compose up -d
docker compose exec mysql mysql -uroot -p12345678 -e "SHOW VARIABLES LIKE 'ngram_token_size'"
# → ngram_token_size  1  이어야 한다. 2로 나오면 my.cnf가 마운트되지 않은 것이다.
curl -s localhost:9200/_cluster/health | grep status
```

### 3. 적재

먼저 **소규모 프로브로 처리율을 재고** 본적재 소요시간을 역산한다. 이 단계를 건너뛰면 수 시간짜리 적재가 막판에 실패할 수 있다.

```bash
DOC_COUNT=5000 npm run load -- both
```

로그의 `처리율 N MB/s`로 10만건 소요시간을 역산한 뒤 본적재한다. 두 엔진을 동시에 돌리면 메모리가 경합하므로 **순차 실행**한다.

```bash
npm run load -- mysql        # 적재 후 ALTER TABLE ADD FULLTEXT (가장 오래 걸리는 구간)
npm run load -- opensearch   # 벌크 적재 후 forcemerge
```

100만건으로 올리려면 `DOC_COUNT=1000000`만 바꾸면 된다. 다만 원문만 22.5GB, 색인 포함 130GB 이상이고 이 문서 작성 시점 하드웨어 기준 10~20시간이 예상된다.

### 4. 엔진 동등성 검증 (게이트)

```bash
npm run parity
```

15개 질의 문자 각각에 대해 두 엔진의 매칭 문서 수를 비교한다. 오차 1%를 넘으면 종료 코드 1로 실패하며, **이 경우 부하테스트를 진행하지 않는다.**

### 5. 부하테스트

```bash
npm run start &
bash scripts/run-load-test.sh
```

2 엔진 × 3 계층 × 2 단계 = 12회 실행, 조합당 워밍업 10초를 포함한다.

| 단계 | 실행기 | 목적 |
| --- | --- | --- |
| `baseline` | `per-vu-iterations`, VU 1, 500회 | 큐잉 없는 순수 쿼리 비용 |
| `ramp` | `ramping-arrival-rate`, 30초 × 5단계 | p95가 무너지는 포화점 탐색 |

고정 VU 대신 도착률 기반을 쓰는 이유는 coordinated omission 때문이다. VU를 고정하면 느린 엔진은 요청을 덜 보낼 뿐이라 지연이 실제보다 낮게 나온다.

계층별 램프 대역은 쿼리 비용 차이(10~100배)를 반영해 다르게 잡았다.

| 계층 | RPS 램프 | 근거 |
| --- | --- | --- |
| `RARE` | 25 → 50 → 100 → 250 → 500 | 여기서 막히면 엔진이 아니라 앱 레이어 천장 |
| `MEDIUM` | 10 → 25 → 50 → 100 → 200 | 실전 서비스 피크 대역 |
| `COMMON` | 2 → 5 → 10 → 25 → 50 | 전 문서 스코어링이라 두 자릿수에서 포화 예상 |

결과는 `k6/results/{engine}-{tier}-{phase}.json`에 저장되고 비교표가 출력된다. 표본이 1,000건 미만이면 p99를 `표본부족`으로 표기하며 통계로 포장하지 않는다.

### 6. API 확인

```bash
curl "localhost:3000/search?engine=mysql&q=다&size=5"
curl "localhost:3000/search?engine=opensearch&q=다&size=5"
open http://localhost:3000/api-docs
```

---

## 최소 부하 동작 검증

벤치마크 수치를 얻는 게 아니라 **파이프라인이 끝까지 도는지만** 확인하는 경로다. 메모리와 실행 시간을 줄이는 환경변수를 쓴다.

| 환경변수 | 벤치마크 기본값 | 검증용 |
| --- | --- | --- |
| `MYSQL_BUFFER_POOL` | `2G` | `128M` |
| `OPENSEARCH_HEAP` | `2g` | `512m` |
| `DOC_COUNT` | `100000` | `1000` |
| `WARMUP_DURATION` | `10s` | `2s` |
| `STAGE_DURATION` | `30s` | `3s` |
| `BASELINE_ITERATIONS` | `500` | `20` |
| `COOLDOWN_SECONDS` | `30` | `2` |

```bash
export MYSQL_BUFFER_POOL=128M OPENSEARCH_HEAP=512m
docker compose up -d

DOC_COUNT=1000 npm run load -- both
DOC_COUNT=1000 npm run parity
npm run start &

WARMUP_DURATION=2s STAGE_DURATION=3s BASELINE_ITERATIONS=20 COOLDOWN_SECONDS=2 \
  bash scripts/run-load-test.sh
```

문서 1,000건이면 `RARE` 마커가 정확히 1건씩(0.1%), `MEDIUM`이 100건씩(10%) 들어가 세 계층이 모두 성립한다. 이보다 줄이면 `RARE` 계층이 0건이 되어 동등성 검증이 무의미해진다.

**이 경로로 얻은 지연 수치는 벤치마크로 쓸 수 없다.** 데이터가 캐시에 전부 들어가고 램프가 3초라 포화점에 닿지 않는다.

---

## 측정 결과

> 미측정. 아래 표는 5번 단계 실행 후 채운다.

| 엔진 | 계층 | 단계 | 표본 | RPS | p50 | p95 | p99 | 실패율 | 판정 |
| --- | --- | --- | --- | --- | --- | --- | --- | --- | --- |
| | | | | | | | | | |

---

## 측정이 중단된 이유

작업 도중 이 머신의 자원 상황이 계획 수립 시점과 달라졌다.

| 지표 | 계획 수립 시 | 중단 시점 |
| --- | --- | --- |
| 가용 RAM | 7.3 GB | **2.0 GB** (free 135 MB) |
| Swap | 5.7 / 8 GB | 5.7 / 8 GB |
| Load average (6코어) | — | **47 ~ 65** |

다른 워크로드(java 3개 약 3.9GB, 기타 프로세스 다수)가 동시에 돌면서 계획이 전제한 4.5GB를 확보할 수 없었다. 이 상태로 컨테이너를 띄우면 스왑으로 밀려나 **측정되는 값이 엔진 특성이 아니라 스왑 대기 시간**이 되므로 벤치마크가 무효가 된다. 참고로 이 시점에 평소 10초 걸리던 TypeScript 컴파일이 5분을 넘겼다.

자원이 확보되면 위 실행 절차 2번부터 그대로 재개하면 된다. 코드와 설정은 모두 완성·검증되어 있다.

---

## 프로젝트 구조

```
opensearch-ngram-poc/
├── docker-compose.yml                    # mysql:8.4, opensearchproject/opensearch:2.19.1
├── infra/mysql/conf/my.cnf               # ngram_token_size=1 등
├── data/
│   ├── korean-syllable-frequency.json    # 프로젝트 덤프 파생 집계 통계 (원문 없음)
│   └── tier-query-characters.json        # 계층별 질의 문자 (생성기·동등성 게이트용)
├── src/
│   ├── novel/                            # 음절 프로파일, 본문 생성기, 선택도 계층
│   ├── search/                           # 검색 API, 두 엔진 Repository
│   └── loader/                           # 적재 CLI
├── scripts/
│   ├── verify-engine-parity.ts           # 동등성 게이트
│   ├── run-load-test.sh                  # 12회 조합 실행
│   └── summarize-load-test.py            # 결과 JSON → 비교표
└── k6/single-char-search.js             # 계층별 질의 문자 사본을 직접 들고 있다
```

본문 생성기는 시드와 문서 인덱스로 완전히 결정적이라, 두 엔진에 각각 적재해도 같은 본문이 들어간다. 덕분에 10만건(2.25GB)을 메모리에 올리지 않고 배치마다 생성할 수 있다.
