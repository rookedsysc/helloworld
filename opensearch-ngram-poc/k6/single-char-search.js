import http from 'k6/http';
import { check } from 'k6';

const ENGINE = __ENV.ENGINE;
const TIER = __ENV.TIER;
const PHASE = __ENV.PHASE;
const BASE_URL = __ENV.BASE_URL || 'http://127.0.0.1:3000';
const RESULT_SIZE = __ENV.RESULT_SIZE || '20';

// 계층별 질의 문자 5종. 같은 글자만 반복하면 버퍼풀과 Lucene 캐시가 완전히 데워져
// 실제보다 빠른 수치가 나오므로 매 요청마다 무작위로 고른다.
// data/tier-query-characters.json의 characters를 옮겨 적은 사본이다. 생성기와 동등성
// 게이트는 그 JSON을 계속 단일 출처로 읽으므로, 한쪽을 바꾸면 다른 쪽도 같이 바꾼다.
const TIER_QUERY_CHARACTERS = {
  common: ['다', '이', '는', '었', '지'],
  medium: ['촉', '뗀', '찜', '솜', '믄'],
  rare: ['냬', '킼', '휙', '빽', '쩝'],
};
const QUERY_CHARACTERS = TIER_QUERY_CHARACTERS[TIER];

// 계층마다 쿼리 비용이 10~100배 차이나므로 도착률 램프를 따로 잡는다.
const RAMP_TARGETS = {
  rare: [25, 50, 100, 250, 500],
  medium: [10, 25, 50, 100, 200],
  common: [2, 5, 10, 25, 50],
};

// 벤치마크 기본값이며, 동작만 확인하는 스모크에서는 환경변수로 낮춰 실행 시간을 줄인다.
const WARMUP_DURATION = __ENV.WARMUP_DURATION || '10s';
const STAGE_DURATION = __ENV.STAGE_DURATION || '30s';
const BASELINE_ITERATIONS = Number(__ENV.BASELINE_ITERATIONS || 500);

// 닫힌 모델(고정 VU)은 느린 엔진이 요청을 덜 보내 지연이 낮게 보이는 coordinated
// omission이 생긴다. 포화점 탐색은 도착률 기반으로 열어 큐 증가가 지연에 반영되게 한다.
const measuredScenario =
  PHASE === 'baseline'
    ? {
        executor: 'per-vu-iterations',
        vus: 1,
        iterations: BASELINE_ITERATIONS,
        maxDuration: '2m',
      }
    : {
        executor: 'ramping-arrival-rate',
        startRate: RAMP_TARGETS[TIER][0],
        timeUnit: '1s',
        preAllocatedVUs: 50,
        maxVUs: 300,
        stages: RAMP_TARGETS[TIER].map((target) => ({ target, duration: STAGE_DURATION })),
      };

export const options = {
  // 요약의 trend 메트릭은 기본적으로 avg/min/med/max/p(90)/p(95)만 담는다.
  // p(99)는 여기에 명시하지 않으면 임계치를 선언해도 값이 나오지 않는다.
  summaryTrendStats: ['avg', 'min', 'med', 'p(95)', 'p(99)', 'max'],
  scenarios: {
    warmup: {
      executor: 'constant-arrival-rate',
      rate: 5,
      timeUnit: '1s',
      duration: WARMUP_DURATION,
      preAllocatedVUs: 10,
      maxVUs: 50,
      exec: 'searchOnce',
      tags: { phase: 'warmup' },
    },
    measured: {
      ...measuredScenario,
      startTime: WARMUP_DURATION,
      exec: 'searchOnce',
      tags: { phase: 'measured' },
    },
  },
  // 워밍업을 제외한 구간만 집계하려면 태그가 붙은 하위 메트릭이 요약에 들어와야 하는데,
  // k6는 threshold가 선언된 하위 메트릭만 요약에 포함한다. duration과 reqs의 항상 참인
  // 조건은 그 용도이고, failed의 조건만 실제 판정에 쓰인다.
  thresholds: {
    'http_req_failed{phase:measured}': ['rate<0.01'],
    'http_req_duration{phase:measured}': ['p(95)>=0'],
    'http_reqs{phase:measured}': ['count>=0'],
  },
};

export function searchOnce() {
  const character = QUERY_CHARACTERS[Math.floor(Math.random() * QUERY_CHARACTERS.length)];
  const url = `${BASE_URL}/search?engine=${ENGINE}&q=${encodeURIComponent(character)}&size=${RESULT_SIZE}`;
  const response = http.get(url);
  check(response, { 'status is 200': (r) => r.status === 200 });
}

export function handleSummary(data) {
  return { stdout: JSON.stringify(data) };
}
