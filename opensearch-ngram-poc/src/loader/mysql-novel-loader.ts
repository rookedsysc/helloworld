import { createConnection, Connection } from 'mysql2/promise';

import { MYSQL_CONNECTION, NOVEL_TABLE_NAME } from '../connection-settings';
import { NovelTextGenerator } from '../novel/novel-text-generator';
import { iterateNovelBatches } from './novel-batch-iterator';
import { LoadProgressReporter } from './load-progress-reporter';

/** 문서당 본문이 최대 1만자(UTF-8 약 30KB)라 배치 하나가 max_allowed_packet 256M 안에 들어오도록 잡는다. */
const INSERT_BATCH_SIZE = 200;
const PROGRESS_REPORT_INTERVAL = 2000;

/**
 * 합성 소설을 MySQL에 적재하고 ngram FULLTEXT 인덱스를 만든다.
 *
 * FULLTEXT 인덱스는 적재를 모두 끝낸 뒤 한 번에 생성한다. 행마다 증분 색인하는
 * 것보다 일괄 정렬 빌드가 빠르고, 1-gram은 토큰 수가 많아 그 차이가 크다.
 */
export class MysqlNovelLoader {
  /**
   * 테이블을 새로 만들고 문서를 적재한 뒤 FULLTEXT 인덱스를 생성한다.
   *
   * @param generator 본문 생성기
   * @param totalDocuments 적재할 문서 수
   * @returns 적재 소요 초와 인덱스 생성 소요 초
   */
  async load(
    generator: NovelTextGenerator,
    totalDocuments: number,
  ): Promise<{ insertSeconds: number; indexSeconds: number }> {
    const connection = await createConnection(MYSQL_CONNECTION);
    try {
      await this.recreateTable(connection);
      const insertSeconds = await this.insertAll(connection, generator, totalDocuments);
      const indexSeconds = await this.createFulltextIndex(connection);
      return { insertSeconds, indexSeconds };
    } finally {
      await connection.end();
    }
  }

  private async recreateTable(connection: Connection): Promise<void> {
    await connection.query(`DROP TABLE IF EXISTS ${NOVEL_TABLE_NAME}`);
    await connection.query(
      `CREATE TABLE ${NOVEL_TABLE_NAME} (
        id      BIGINT UNSIGNED NOT NULL,
        title   VARCHAR(200) NOT NULL,
        content LONGTEXT     NOT NULL,
        PRIMARY KEY (id)
      ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci`,
    );
  }

  private async insertAll(
    connection: Connection,
    generator: NovelTextGenerator,
    totalDocuments: number,
  ): Promise<number> {
    const reporter = new LoadProgressReporter('MySQL 적재', totalDocuments, PROGRESS_REPORT_INTERVAL);

    for (const batch of iterateNovelBatches(generator, totalDocuments, INSERT_BATCH_SIZE)) {
      const rows = batch.map((novel) => [novel.id, novel.title, novel.content]);
      await connection.query(`INSERT INTO ${NOVEL_TABLE_NAME} (id, title, content) VALUES ?`, [rows]);
      reporter.recordBatch(
        batch.length,
        batch.reduce((sum, novel) => sum + novel.content.length, 0),
      );
    }

    return reporter.finish().elapsedSeconds;
  }

  private async createFulltextIndex(connection: Connection): Promise<number> {
    console.log('[MySQL 인덱스] ngram FULLTEXT 인덱스 생성 시작 (진행률 표시 없음, 완료까지 대기)');
    const startedAt = Date.now();
    await connection.query(
      `ALTER TABLE ${NOVEL_TABLE_NAME} ADD FULLTEXT INDEX ft_novel_content (content) WITH PARSER ngram`,
    );
    const indexSeconds = (Date.now() - startedAt) / 1000;
    console.log(`[MySQL 인덱스] 완료 — 소요 ${indexSeconds.toFixed(1)}초 (${(indexSeconds / 60).toFixed(1)}분)`);
    return indexSeconds;
  }
}
