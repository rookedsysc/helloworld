import { Controller, Get, Query } from '@nestjs/common';
import { ApiBadRequestResponse, ApiOkResponse, ApiOperation, ApiTags } from '@nestjs/swagger';

import { NovelSearchRequest } from './dto/novel-search.request';
import { NovelSearchItem, NovelSearchResponse } from './dto/novel-search.response';
import { MysqlNovelSearchRepository } from './mysql-novel-search.repository';
import { OpensearchNovelSearchRepository } from './opensearch-novel-search.repository';
import { SearchEngine } from './search-engine';

const MILLISECONDS_PER_SECOND = 1000;

@ApiTags('소설 검색')
@Controller('search')
export class SearchController {
  constructor(
    private readonly mysqlRepository: MysqlNovelSearchRepository,
    private readonly opensearchRepository: OpensearchNovelSearchRepository,
  ) {}

  @Get()
  @ApiOperation({
    summary: '1글자 소설 본문 검색',
    description:
      '지정한 엔진에서 한 글자를 본문 전문검색으로 조회합니다. MySQL은 ngram_token_size=1 FULLTEXT, OpenSearch는 min_gram=max_gram=1 ngram 분석기를 사용합니다.',
  })
  @ApiOkResponse({ description: '검색 성공', type: NovelSearchResponse })
  @ApiBadRequestResponse({
    description: '지원하지 않는 engine 값이거나 q가 한 글자가 아닌 경우',
  })
  async search(@Query() request: NovelSearchRequest): Promise<NovelSearchResponse> {
    const startedAt = process.hrtime.bigint();
    const items = await this.searchWithEngine(request.engine, request.q, request.size);
    const elapsedNanoseconds = Number(process.hrtime.bigint() - startedAt);

    return {
      engine: request.engine,
      query: request.q,
      tookMs: elapsedNanoseconds / MILLISECONDS_PER_SECOND / MILLISECONDS_PER_SECOND,
      items,
    };
  }

  private searchWithEngine(
    engine: SearchEngine,
    query: string,
    size: number,
  ): Promise<NovelSearchItem[]> {
    if (engine === SearchEngine.MYSQL) {
      return this.mysqlRepository.search(query, size);
    }
    return this.opensearchRepository.search(query, size);
  }
}
