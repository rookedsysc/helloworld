import { MysqlNovelSearchRepository } from '../src/search/mysql-novel-search.repository';
import { OpensearchNovelSearchRepository } from '../src/search/opensearch-novel-search.repository';
import { SelectivityTier } from '../src/novel/selectivity-tier';
import { loadTierQueryCharacters } from '../src/novel/tier-query-characters';
import { DOCUMENT_COUNT } from '../src/connection-settings';

/** 두 엔진이 같은 문서 집합을 본다고 인정할 매칭 건수 오차 상한. */
const MATCH_COUNT_TOLERANCE = 0.01;

/**
 * 부하테스트 전에 두 엔진이 같은 문서 집합을 보는지 확인한다.
 *
 * 토큰 단위가 어긋난 채로 측정하면 두 엔진이 서로 다른 일을 하는 것이라 지연
 * 비교가 성립하지 않는다. 이 스크립트가 실패하면 벤치마크를 진행하지 않는다.
 */
async function main(): Promise<void> {
  const tierCharacters = loadTierQueryCharacters();
  const mysqlRepository = new MysqlNovelSearchRepository();
  const opensearchRepository = new OpensearchNovelSearchRepository();

  let hasMismatch = false;
  console.log('계층\t문자\tMySQL\t\tOpenSearch\t오차\t목표포함률\t판정');

  for (const tier of Object.values(SelectivityTier)) {
    const { characters, targetDocumentRatio } = tierCharacters[tier];
    for (const character of characters) {
      const [mysqlCount, opensearchCount] = await Promise.all([
        mysqlRepository.countMatches(character),
        opensearchRepository.countMatches(character),
      ]);

      const larger = Math.max(mysqlCount, opensearchCount, 1);
      const relativeDifference = Math.abs(mysqlCount - opensearchCount) / larger;
      const isWithinTolerance = relativeDifference <= MATCH_COUNT_TOLERANCE;
      hasMismatch = hasMismatch || !isWithinTolerance;

      const expectedCount = Math.round(DOCUMENT_COUNT * targetDocumentRatio);
      console.log(
        `${tier}\t${character}\t${mysqlCount.toLocaleString()}\t\t${opensearchCount.toLocaleString()}\t` +
          `${(relativeDifference * 100).toFixed(2)}%\t${expectedCount.toLocaleString()}\t\t${isWithinTolerance ? 'PASS' : 'FAIL'}`,
      );
    }
  }

  await mysqlRepository.onModuleDestroy();

  if (hasMismatch) {
    console.error(`\n오차 ${MATCH_COUNT_TOLERANCE * 100}%를 넘는 문자가 있어 벤치마크를 진행할 수 없습니다.`);
    process.exit(1);
  }
  console.log('\n두 엔진의 매칭 문서 수가 모두 허용 오차 안입니다. 벤치마크를 진행할 수 있습니다.');
}

main().catch((error) => {
  console.error(error);
  process.exit(1);
});
