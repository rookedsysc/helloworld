import { Injectable } from '@nestjs/common';
import { Client } from '@opensearch-project/opensearch';

import { NOVEL_INDEX_NAME, OPENSEARCH_NODE } from '../connection-settings';
import { NovelSearchItem } from './dto/novel-search.response';

/**
 * OpenSearch 1-gram 분석기로 1글자 검색을 수행한다.
 *
 * 색인은 ngram tokenizer를 min_gram=max_gram=1로 설정해 MySQL ngram(1)과
 * 토큰 단위를 맞췄으므로, match 질의 하나가 단일 term 조회가 된다.
 */
@Injectable()
export class OpensearchNovelSearchRepository {
  private readonly client = new Client({ node: OPENSEARCH_NODE });

  /**
   * 검색어를 포함한 문서를 관련도 순으로 조회한다.
   *
   * @param query 검색할 한 글자
   * @param size 반환할 최대 문서 수
   * @returns 매칭된 소설의 식별자와 제목
   */
  async search(query: string, size: number): Promise<NovelSearchItem[]> {
    const response = await this.client.search({
      index: NOVEL_INDEX_NAME,
      body: { query: { match: { content: query } } },
      size,
      _source: ['title'],
    });

    // 클라이언트 타입은 _source만 제네릭으로 노출해 _id를 포함하지 않으므로 실제 응답 형태로 좁힌다.
    const hits = response.body.hits.hits as unknown as Array<{ _id: string; _source: { title: string } }>;
    return hits.map((hit) => ({ id: Number(hit._id), title: hit._source.title }));
  }

  /**
   * 검색어에 매칭되는 전체 문서 수를 센다.
   *
   * 동등성 스모크 전용이며 부하테스트 경로에서는 쓰지 않는다.
   *
   * @param query 검색할 한 글자
   * @returns 매칭 문서 수
   */
  async countMatches(query: string): Promise<number> {
    const response = await this.client.count({
      index: NOVEL_INDEX_NAME,
      body: { query: { match: { content: query } } },
    });
    return response.body.count;
  }
}
