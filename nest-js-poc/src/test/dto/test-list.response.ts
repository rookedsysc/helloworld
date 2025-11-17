import { ApiProperty, PickType } from '@nestjs/swagger';
import { TestDetailResponse } from './test-detail.response';

export class TestListResponse extends PickType(TestDetailResponse, [
  'id',
  'title',
] as const) {
  @ApiProperty({
    description: '테스트 ID',
    example: 1,
  })
  id: number;

  @ApiProperty({
    description: '테스트 제목',
    example: 'Test Title',
  })
  title: string;

  constructor({ id, title }: { id: number; title: string }) {
    super();
    this.id = id;
    this.title = title;
  }
}
