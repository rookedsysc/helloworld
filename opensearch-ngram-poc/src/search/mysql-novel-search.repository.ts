import { Injectable, OnModuleDestroy } from '@nestjs/common';
import { createPool, Pool, RowDataPacket } from 'mysql2/promise';

import { MYSQL_CONNECTION, NOVEL_TABLE_NAME } from '../connection-settings';
import { NovelSearchItem } from './dto/novel-search.response';

/** 부하테스트가 최대 200 RPS까지 올라가므로 큐 대기가 병목이 되지 않을 만큼 확보한다. */
const CONNECTION_LIMIT = 50;

interface NovelRow extends RowDataPacket {
  id: number;
  title: string;
}

interface CountRow extends RowDataPacket {
  matchCount: number;
}

/**
 * MySQL FULLTEXT ngram 파서로 1글자 검색을 수행한다.
 *
 * ngram_token_size=1 환경에서 BOOLEAN MODE를 쓰면 검색어가 단일 ngram 토큰으로
 * 변환되어 정확히 그 문자를 포함한 문서만 매칭된다.
 */
@Injectable()
export class MysqlNovelSearchRepository implements OnModuleDestroy {
  private readonly pool: Pool = createPool({
    ...MYSQL_CONNECTION,
    connectionLimit: CONNECTION_LIMIT,
    waitForConnections: true,
  });

  onModuleDestroy(): Promise<void> {
    return this.pool.end();
  }

  /**
   * 검색어를 포함한 문서를 관련도 순으로 조회한다.
   *
   * @param query 검색할 한 글자
   * @param size 반환할 최대 문서 수
   * @returns 매칭된 소설의 식별자와 제목
   */
  async search(query: string, size: number): Promise<NovelSearchItem[]> {
    const [rows] = await this.pool.execute<NovelRow[]>(
      `SELECT id, title FROM ${NOVEL_TABLE_NAME} WHERE MATCH(content) AGAINST (? IN BOOLEAN MODE) LIMIT ?`,
      [query, size],
    );
    return rows.map((row) => ({ id: row.id, title: row.title }));
  }

  /**
   * 검색어에 매칭되는 전체 문서 수를 센다.
   *
   * 두 엔진이 같은 문서 집합을 보는지 확인하는 동등성 스모크 전용이다.
   * 전수 스캔을 유발하므로 부하테스트 경로에서는 쓰지 않는다.
   *
   * @param query 검색할 한 글자
   * @returns 매칭 문서 수
   */
  async countMatches(query: string): Promise<number> {
    const [rows] = await this.pool.execute<CountRow[]>(
      `SELECT COUNT(*) AS matchCount FROM ${NOVEL_TABLE_NAME} WHERE MATCH(content) AGAINST (? IN BOOLEAN MODE)`,
      [query],
    );
    return Number(rows[0].matchCount);
  }
}
