import { GeneratedNovel, NovelTextGenerator } from '../novel/novel-text-generator';

/** 식별자가 붙은 소설 한 편. 두 엔진이 같은 식별자를 쓰도록 문서 순번에서 유도한다. */
export interface IdentifiedNovel extends GeneratedNovel {
  id: number;
}

/**
 * 소설을 배치 단위로 만들어 넘긴다.
 *
 * 10만건 전체를 메모리에 올리면 2.25GB라 적재 중 OOM이 나므로 배치마다 생성한다.
 * 생성기가 문서 인덱스로 결정적이라 두 엔진이 독립 실행해도 같은 본문을 얻는다.
 *
 * @param generator 본문 생성기
 * @param totalDocuments 만들 문서 수
 * @param batchSize 한 번에 넘길 문서 수
 */
export function* iterateNovelBatches(
  generator: NovelTextGenerator,
  totalDocuments: number,
  batchSize: number,
): Generator<IdentifiedNovel[]> {
  for (let batchStart = 0; batchStart < totalDocuments; batchStart += batchSize) {
    const batchEnd = Math.min(batchStart + batchSize, totalDocuments);
    const batch: IdentifiedNovel[] = [];
    for (let documentIndex = batchStart; documentIndex < batchEnd; documentIndex += 1) {
      batch.push({ id: documentIndex + 1, ...generator.generate(documentIndex) });
    }
    yield batch;
  }
}
