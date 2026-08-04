import { Client } from '@opensearch-project/opensearch';

import { NOVEL_INDEX_NAME, OPENSEARCH_NODE } from '../connection-settings';
import { NovelTextGenerator } from '../novel/novel-text-generator';
import { iterateNovelBatches } from './novel-batch-iterator';
import { LoadProgressReporter } from './load-progress-reporter';

const BULK_BATCH_SIZE = 200;
const PROGRESS_REPORT_INTERVAL = 2000;

/**
 * MySQL ngram(1)과 토큰 단위를 맞춘 색인 설정.
 *
 * min_gram과 max_gram을 모두 1로 두면 차이가 0이라 index.max_ngram_diff(기본 1)를
 * 건드릴 필요가 없다. MySQL ngram 파서가 공백을 제거하므로 token_chars를
 * letter와 digit으로 제한해 공백과 문장부호를 같은 방식으로 배제한다.
 */
const NOVEL_INDEX_DEFINITION = {
  settings: {
    index: {
      number_of_shards: 1,
      number_of_replicas: 0,
      // 적재 중에는 refresh를 끄고 완료 후 되돌린다.
      refresh_interval: '-1',
    },
    analysis: {
      tokenizer: {
        single_char_ngram: {
          type: 'ngram' as const,
          min_gram: 1,
          max_gram: 1,
          token_chars: ['letter' as const, 'digit' as const],
        },
      },
      analyzer: {
        single_char: { type: 'custom' as const, tokenizer: 'single_char_ngram' },
      },
    },
  },
  mappings: {
    properties: {
      title: { type: 'keyword' as const },
      content: { type: 'text' as const, analyzer: 'single_char' },
    },
  },
};

/** 합성 소설을 OpenSearch 1-gram 색인에 적재한다. */
export class OpensearchNovelLoader {
  private readonly client = new Client({ node: OPENSEARCH_NODE });

  /**
   * 색인을 새로 만들고 문서를 벌크 적재한 뒤 검색 조건을 안정화한다.
   *
   * @param generator 본문 생성기
   * @param totalDocuments 적재할 문서 수
   * @returns 적재 소요 초와 forcemerge 소요 초
   */
  async load(
    generator: NovelTextGenerator,
    totalDocuments: number,
  ): Promise<{ insertSeconds: number; mergeSeconds: number }> {
    await this.recreateIndex();
    const insertSeconds = await this.bulkAll(generator, totalDocuments);
    const mergeSeconds = await this.stabilizeForSearch();
    return { insertSeconds, mergeSeconds };
  }

  private async recreateIndex(): Promise<void> {
    const { body: exists } = await this.client.indices.exists({ index: NOVEL_INDEX_NAME });
    if (exists) {
      await this.client.indices.delete({ index: NOVEL_INDEX_NAME });
    }
    await this.client.indices.create({ index: NOVEL_INDEX_NAME, body: NOVEL_INDEX_DEFINITION });
  }

  private async bulkAll(generator: NovelTextGenerator, totalDocuments: number): Promise<number> {
    const reporter = new LoadProgressReporter('OpenSearch 적재', totalDocuments, PROGRESS_REPORT_INTERVAL);

    for (const batch of iterateNovelBatches(generator, totalDocuments, BULK_BATCH_SIZE)) {
      const operations = batch.flatMap((novel) => [
        { index: { _index: NOVEL_INDEX_NAME, _id: String(novel.id) } },
        { title: novel.title, content: novel.content },
      ]);
      const { body: response } = await this.client.bulk({ body: operations });
      if (response.errors) {
        const firstError = response.items.find((item: { index?: { error?: unknown } }) => item.index?.error);
        throw new Error(`벌크 적재 실패: ${JSON.stringify(firstError)}`);
      }
      reporter.recordBatch(
        batch.length,
        batch.reduce((sum, novel) => sum + novel.content.length, 0),
      );
    }

    return reporter.finish().elapsedSeconds;
  }

  /**
   * 검색 전 색인 상태를 고정한다.
   *
   * 세그먼트 수가 다르면 질의 비용이 달라져 재현성이 떨어지므로 하나로 병합한다.
   */
  private async stabilizeForSearch(): Promise<number> {
    console.log('[OpenSearch 인덱스] refresh 복원 및 forcemerge 시작');
    const startedAt = Date.now();
    await this.client.indices.putSettings({
      index: NOVEL_INDEX_NAME,
      body: { index: { refresh_interval: '1s' } },
    });
    await this.client.indices.refresh({ index: NOVEL_INDEX_NAME });
    await this.client.indices.forcemerge({ index: NOVEL_INDEX_NAME, max_num_segments: 1 });
    const mergeSeconds = (Date.now() - startedAt) / 1000;
    console.log(`[OpenSearch 인덱스] 완료 — 소요 ${mergeSeconds.toFixed(1)}초`);
    return mergeSeconds;
  }
}
