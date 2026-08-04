import { ApiProperty, ApiPropertyOptional } from '@nestjs/swagger';
import { Type } from 'class-transformer';
import { IsEnum, IsInt, IsOptional, IsString, Length, Max, Min } from 'class-validator';

import { SearchEngine } from '../search-engine';

const DEFAULT_RESULT_SIZE = 20;
const MAX_RESULT_SIZE = 100;

export class NovelSearchRequest {
  @ApiProperty({
    description: '검색을 수행할 엔진',
    enum: SearchEngine,
    enumName: 'SearchEngine',
    example: SearchEngine.MYSQL,
    required: true,
  })
  @IsEnum(SearchEngine)
  engine!: SearchEngine;

  @ApiProperty({
    description: '검색할 문자. MySQL ngram_token_size=1과 OpenSearch 1-gram을 비교하므로 정확히 한 글자만 받는다.',
    example: '다',
    required: true,
    minLength: 1,
    maxLength: 1,
  })
  @IsString()
  @Length(1, 1)
  q!: string;

  @ApiPropertyOptional({
    description: '반환할 최대 문서 수',
    example: DEFAULT_RESULT_SIZE,
    default: DEFAULT_RESULT_SIZE,
    minimum: 1,
    maximum: MAX_RESULT_SIZE,
  })
  @Type(() => Number)
  @IsInt()
  @Min(1)
  @Max(MAX_RESULT_SIZE)
  @IsOptional()
  size: number = DEFAULT_RESULT_SIZE;
}
