#!/usr/bin/env bash
# 두 엔진 x 세 선택도 계층 x 두 단계로 1글자 검색 부하테스트를 실행한다.
#
# k6가 로컬에 설치되어 있지 않으므로 컨테이너로 실행한다. 컨테이너 안에는 스크립트
# 파일이 없어 stdin으로 넘긴다. 계층별 질의 문자는 k6 스크립트가 직접 들고 있다.
set -euo pipefail

PROJECT_ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
RESULT_DIR="${PROJECT_ROOT}/k6/results"
BASE_URL="${BASE_URL:-http://127.0.0.1:3000}"
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
    for phase in baseline ramp; do
      result_file="${RESULT_DIR}/${engine}-${tier}-${phase}.json"
      echo "▶ ${engine} / ${tier} / ${phase}"
      k6_exit=0
      docker run --rm -i --network host \
        -e "ENGINE=${engine}" \
        -e "TIER=${tier}" \
        -e "PHASE=${phase}" \
        -e "BASE_URL=${BASE_URL}" \
        -e "WARMUP_DURATION=${WARMUP_DURATION:-10s}" \
        -e "STAGE_DURATION=${STAGE_DURATION:-30s}" \
        -e "BASELINE_ITERATIONS=${BASELINE_ITERATIONS:-500}" \
        grafana/k6 run --quiet - < "${PROJECT_ROOT}/k6/single-char-search.js" > "${result_file}" || k6_exit=$?
      # k6는 임계치를 넘기면 종료 코드 99로 끝난다. 포화점을 찾는 것이 이 스윕의 목적이라
      # 임계치 초과는 실패가 아니라 측정 결과이므로, set -e가 남은 조합과 요약 단계까지
      # 중단시키지 않도록 99만 흡수한다. 어떤 조합이 무너졌는지는 요약표의 실패율과 판정
      # 열이 그대로 보여준다. 그 밖의 종료 코드는 결과 JSON 자체가 없다는 뜻이라 중단시킨다.
      if [[ "${k6_exit}" -eq 99 ]]; then
        echo "  임계치 초과 — 포화 구간으로 기록하고 계속 진행합니다 (${result_file##*/})"
      elif [[ "${k6_exit}" -ne 0 ]]; then
        echo "k6 실행이 실패했습니다: 종료 코드 ${k6_exit} (${engine}/${tier}/${phase})" >&2
        exit "${k6_exit}"
      fi
      sleep "${COOLDOWN_SECONDS}"
    done
  done
done

python3 "${PROJECT_ROOT}/scripts/summarize-load-test.py" "${RESULT_DIR}"
