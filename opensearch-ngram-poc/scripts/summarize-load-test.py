"""k6 결과 JSON들을 하나의 비교표로 정리한다.

워밍업 구간을 제외한 phase:measured 하위 메트릭만 읽는다. 임계치를 넘긴 조합은
성공으로 포장하지 않고 포화로 표기한다.

사용법:
    python3 scripts/summarize-load-test.py <결과디렉토리>
"""
import json
import pathlib
import sys

# 표본이 이 수에 못 미치면 p99를 신뢰할 수 없어 보고하지 않는다.
MIN_SAMPLES_FOR_P99 = 1000


def read_measured_metrics(result_path):
    """k6 요약에서 워밍업을 제외한 측정 구간 메트릭을 뽑는다."""
    summary = json.loads(result_path.read_text(encoding="utf-8"))
    metrics = summary.get("metrics", {})
    duration = metrics.get("http_req_duration{phase:measured}", {}).get("values", {})
    failed = metrics.get("http_req_failed{phase:measured}", {}).get("values", {})
    requests = metrics.get("http_reqs{phase:measured}", {}).get("values", {})
    return duration, failed, requests


def format_latency(value, absent_label):
    """지연 값을 표에 넣을 문자열로 바꾼다. 값이 없으면 0으로 꾸미지 않고 사유를 적는다."""
    return f"{value:.1f}" if value is not None else absent_label


def main():
    if len(sys.argv) != 2:
        print(__doc__)
        sys.exit(2)

    result_dir = pathlib.Path(sys.argv[1])
    rows = []
    for result_path in sorted(result_dir.glob("*.json")):
        engine, tier, phase = result_path.stem.split("-")
        duration, failed, requests = read_measured_metrics(result_path)
        sample_count = int(requests.get("count", 0))
        failure_rate = failed.get("rate", 0.0)
        rows.append(
            {
                "engine": engine,
                "tier": tier,
                "phase": phase,
                "samples": sample_count,
                "rps": requests.get("rate", 0.0),
                # k6는 중앙값을 med로 담는다. p(95)/p(99)는 스크립트의 summaryTrendStats 선언으로 들어온다.
                "p50": duration.get("med"),
                "p95": duration.get("p(95)"),
                "p99": duration.get("p(99)") if sample_count >= MIN_SAMPLES_FOR_P99 else None,
                "failureRate": failure_rate,
                "verdict": "포화" if failure_rate >= 0.01 else "정상",
            }
        )

    header = f"{'엔진':<12}{'계층':<9}{'단계':<10}{'표본':>8}{'RPS':>9}{'p50(ms)':>10}{'p95(ms)':>10}{'p99(ms)':>10}{'실패율':>9}  판정"
    print(header)
    print("-" * len(header))
    for row in rows:
        p50 = format_latency(row["p50"], "값없음")
        p95 = format_latency(row["p95"], "값없음")
        p99 = format_latency(row["p99"], "표본부족")
        print(
            f"{row['engine']:<12}{row['tier']:<9}{row['phase']:<10}{row['samples']:>8,}"
            f"{row['rps']:>9.1f}{p50:>10}{p95:>10}{p99:>10}"
            f"{row['failureRate'] * 100:>8.2f}%  {row['verdict']}"
        )


if __name__ == "__main__":
    main()
