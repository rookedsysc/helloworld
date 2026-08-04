import { ApiProperty } from '@nestjs/swagger';

import { SearchEngine } from '../search-engine';

export class NovelSearchItem {
  @ApiProperty({ description: '소설 식별자', example: 1 })
  id!: number;

  @ApiProperty({ description: '소설 제목', example: '그날의 바람이었다' })
  title!: string;
}

export class NovelSearchResponse {
  @ApiProperty({
    description: '검색을 수행한 엔진',
    enum: SearchEngine,
    enumName: 'SearchEngine',
    example: SearchEngine.MYSQL,
  })
  engine!: SearchEngine;

  @ApiProperty({ description: '검색에 사용한 문자', example: '다' })
  query!: string;

  @ApiProperty({
    description: '엔진 왕복에 걸린 시간(밀리초). 애플리케이션 직렬화 시간은 제외한 순수 질의 시간이다.',
    example: 12.4,
  })
  tookMs!: number;

  @ApiProperty({ description: '검색 결과 목록', type: [NovelSearchItem] })
  items!: NovelSearchItem[];
}
