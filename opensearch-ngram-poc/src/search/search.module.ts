import { Module } from '@nestjs/common';

import { MysqlNovelSearchRepository } from './mysql-novel-search.repository';
import { OpensearchNovelSearchRepository } from './opensearch-novel-search.repository';
import { SearchController } from './search.controller';

@Module({
  controllers: [SearchController],
  providers: [MysqlNovelSearchRepository, OpensearchNovelSearchRepository],
})
export class SearchModule {}
