import { readFileSync } from 'node:fs';
import { join } from 'node:path';

import { SelectivityTier } from './selectivity-tier';

/** 한 선택도 계층의 질의 문자와 목표 포함률. */
export interface TierQueryCharacters {
  characters: string[];
  targetDocumentRatio: number;
  origin: 'natural' | 'injected';
}

const TIER_QUERY_CHARACTERS_PATH = join(__dirname, '..', '..', 'data', 'tier-query-characters.json');

/**
 * 계층별 질의 문자 정의를 읽는다.
 *
 * 생성기와 동등성 스모크, k6 실행 래퍼가 같은 정의를 봐야 하므로 JSON 하나를 단일 출처로 둔다.
 *
 * @returns 계층별 질의 문자와 목표 포함률
 */
export function loadTierQueryCharacters(): Record<SelectivityTier, TierQueryCharacters> {
  return JSON.parse(readFileSync(TIER_QUERY_CHARACTERS_PATH, 'utf-8'));
}

/**
 * 본문 음절 풀에서 배제해야 할 마커를 모은다.
 *
 * 주입으로 포함률을 통제하려면 해당 문자가 본문에 우연히 등장해서는 안 된다.
 * COMMON 계층은 자연 빈도를 그대로 쓰므로 배제 대상이 아니다.
 *
 * @param tierCharacters 계층별 질의 문자 정의
 * @returns 배제할 마커 문자 목록
 */
export function collectInjectedMarkers(
  tierCharacters: Record<SelectivityTier, TierQueryCharacters>,
): string[] {
  return Object.values(tierCharacters)
    .filter((tier) => tier.origin === 'injected')
    .flatMap((tier) => tier.characters);
}
