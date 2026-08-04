"""팬픽 덤프에서 한국어 음절 빈도 프로파일을 추출한다.

1-gram 색인은 문서별 문자 다중집합만 보므로, 합성 본문이 실제 한국어와 같은
Zipf 분포를 따르게 하려면 음절 빈도만 있으면 충분하다. 전이확률(bigram)은
색인 특성에 영향을 주지 않아 추출하지 않는다.

출력에는 집계 수치만 담기며 원문 문장이나 사용자 식별자는 저장하지 않는다.

사용법:
    python3 scripts/extract-syllable-frequency.py <덤프경로> <출력JSON경로>
"""
import collections
import json
import re
import sys

# 소설 산문이 실제로 들어있는 테이블. 나머지는 ID·통계·로그라 분포를 왜곡시킨다.
PROSE_TABLES = {
    "FanficEpisodeTranslation",
    "photocard_fanfic_episode_list",
    "FanficTemporaryEpisode",
}

INSERT_PATTERN = re.compile(r"^INSERT INTO `([^`]+)`")
# 한글 음절과 그 사이 공백까지를 하나의 산문 덩어리로 본다.
PROSE_RUN_PATTERN = re.compile(r"[가-힣][가-힣 ]*")


def extract_profile(dump_path):
    """덤프를 한 줄씩 읽어 산문 테이블에서만 음절 빈도와 공백 수를 집계한다.

    :param dump_path: mysqldump 결과 파일 경로
    :return: 음절 빈도 Counter와 공백 문자 수
    """
    syllable_counts = collections.Counter()
    space_count = 0
    current_table = ""

    with open(dump_path, encoding="utf-8", errors="replace") as dump_file:
        for line in dump_file:
            matched_insert = INSERT_PATTERN.match(line)
            if matched_insert:
                current_table = matched_insert.group(1)
            if current_table not in PROSE_TABLES:
                continue
            for prose_run in PROSE_RUN_PATTERN.findall(line):
                syllable_counts.update(character for character in prose_run if character != " ")
                space_count += prose_run.count(" ")

    return syllable_counts, space_count


def main():
    if len(sys.argv) != 3:
        print(__doc__)
        sys.exit(2)

    dump_path, output_path = sys.argv[1], sys.argv[2]
    syllable_counts, space_count = extract_profile(dump_path)
    total_syllables = sum(syllable_counts.values())

    if total_syllables == 0:
        print(f"산문을 찾지 못했습니다: {dump_path}", file=sys.stderr)
        sys.exit(1)

    profile = {
        "sourceDescription": "fanplus 팬픽 산문 3개 테이블에서 추출한 음절 빈도 집계",
        "totalSyllables": total_syllables,
        "distinctSyllables": len(syllable_counts),
        # 음절 하나당 공백 개수. 합성 본문의 공백 밀도를 실제 산문에 맞추는 데 쓴다.
        "spaceRatio": round(space_count / total_syllables, 6),
        # 빈도 내림차순으로 정렬해 생성기가 누적분포를 그대로 만들 수 있게 한다.
        "syllableFrequencies": dict(syllable_counts.most_common()),
    }

    with open(output_path, "w", encoding="utf-8") as output_file:
        json.dump(profile, output_file, ensure_ascii=False, indent=1)

    print(f"총 음절      : {total_syllables:,}")
    print(f"고유 음절    : {len(syllable_counts):,}")
    print(f"공백 비율    : {profile['spaceRatio']}")
    print(f"저장 완료    : {output_path}")


if __name__ == "__main__":
    main()
