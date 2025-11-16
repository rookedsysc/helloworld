import { PickType } from '@nestjs/swagger';
import { TestDetailResponse } from './test-detail.response';

export class TestListResponse extends PickType(TestDetailResponse, [
  'id',
  'title',
] as const) {
  constructor({ id, title }: { id: number; title: string }) {
    super();
    this.id = id;
    this.title = title;
  }
}
