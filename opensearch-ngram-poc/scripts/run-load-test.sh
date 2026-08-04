#!/usr/bin/env bash
# 두 엔진 x 세 선택도 계층 x 두 단계로 1글자 검색 부하테스트를 실행한다.
#
# k6가 로컬에 설치되어 있지 않으므로 컨테이너로 실행한다. 컨테이너 안에는 스크립트
# 파일이 없어 stdin으로 넘긴다. 계층별 질의 문자는 생성기와 같은 JSON을 단일 출처로 읽는다.
set -euo pipefail

PROJECT_ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
RESULT_DIR="${PROJECT_ROOT}/k6/results"
BASE_URL="${BASE_URL:-http://127.0.0.1:3000}"
TIER_FILE="${PROJECT_ROOT}/data/tier-query-characters.json"
# 다음 조합이 앞 조합의 캐시 상태를 물려받지 않도록 쉬는 시간. 스모크에서는 짧게 줄인다.
COOLDOWN_SECONDS="${COOLDOWN_SECONDS:-30}"

mkdir -p "${RESULT_DIR}"

if ! curl -sf "${BASE_URL}/search?engine=mysql&q=%EB%8B%A4&size=1" > /dev/null; then
  echo "검색 API에 연결할 수 없습니다: ${BASE_URL}" >&2
  echo "npm run start 로 애플리케이션을 먼저 띄우세요." >&2
  exit 1
fi

for engine in mysql opensearch; do
  for tier in common medium rare; do
    characters="$(python3 -c "import json,sys; print(json.dumps(json.load(open('${TIER_FILE}',encoding='utf-8'))['${tier}']['characters'],ensure_ascii=False))")"
    for phase in baseline ramp; do
      result_file="${RESULT_DIR}/${engine}-${tier}-${phase}.json"
      echo "▶ ${engine} / ${tier} / ${phase}"
      docker run --rm -i --network host \
        -e "ENGINE=${engine}" \
        -e "TIER=${tier}" \
        -e "PHASE=${phase}" \
        -e "BASE_URL=${BASE_URL}" \
        -e "QUERY_CHARACTERS=${characters}" \
        -e "WARMUP_DURATION=${WARMUP_DURATION:-10s}" \
        -e "STAGE_DURATION=${STAGE_DURATION:-30s}" \
        -e "BASELINE_ITERATIONS=${BASELINE_ITERATIONS:-500}" \
        grafana/k6 run --quiet - < "${PROJECT_ROOT}/k6/single-char-search.js" > "${result_file}"
      sleep "${COOLDOWN_SECONDS}"
    done
  done
done

python3 "${PROJECT_ROOT}/scripts/summarize-load-test.py" "${RESULT_DIR}"
