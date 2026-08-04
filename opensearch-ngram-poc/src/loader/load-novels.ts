import { DOCUMENT_COUNT, GENERATOR_SEED } from '../connection-settings';
import { KoreanSyllableProfile } from '../novel/korean-syllable-profile';
import { NovelTextGenerator } from '../novel/novel-text-generator';
import { collectInjectedMarkers, loadTierQueryCharacters } from '../novel/tier-query-characters';
import { MysqlNovelLoader } from './mysql-novel-loader';
import { OpensearchNovelLoader } from './opensearch-novel-loader';

type LoadTarget = 'mysql' | 'opensearch' | 'both';

const USAGE = `사용법: npm run load -- <mysql|opensearch|both>
환경변수 DOC_COUNT로 문서 수를, GENERATOR_SEED로 생성 시드를 바꿀 수 있습니다.`;

/**
 * 두 엔진에 같은 합성 소설을 적재한다.
 *
 * 생성기가 문서 인덱스로 결정적이라 엔진별로 따로 실행해도 같은 본문이 들어간다.
 * 두 엔진을 동시에 적재하면 6코어 환경에서 메모리와 CPU가 경합하므로 순차로 돈다.
 */
async function main(): Promise<void> {
  const target = (process.argv[2] ?? 'both') as LoadTarget;
  if (!['mysql', 'opensearch', 'both'].includes(target)) {
    console.error(USAGE);
    process.exit(2);
  }

  const tierCharacters = loadTierQueryCharacters();
  const profile = KoreanSyllableProfile.load(collectInjectedMarkers(tierCharacters));
  const generator = new NovelTextGenerator(profile, tierCharacters, GENERATOR_SEED);

  console.log(
    `적재 대상 ${target} / 문서 ${DOCUMENT_COUNT.toLocaleString()}건 / 시드 ${GENERATOR_SEED} / ` +
      `본문 음절 풀 ${profile.distinctSyllableCount}종`,
  );

  if (target === 'mysql' || target === 'both') {
    const result = await new MysqlNovelLoader().load(generator, DOCUMENT_COUNT);
    console.log(`MySQL 총계 — 적재 ${result.insertSeconds.toFixed(1)}초 + 인덱스 ${result.indexSeconds.toFixed(1)}초`);
  }

  if (target === 'opensearch' || target === 'both') {
    const result = await new OpensearchNovelLoader().load(generator, DOCUMENT_COUNT);
    console.log(`OpenSearch 총계 — 적재 ${result.insertSeconds.toFixed(1)}초 + 병합 ${result.mergeSeconds.toFixed(1)}초`);
  }
}

main().catch((error) => {
  console.error(error);
  process.exit(1);
});
