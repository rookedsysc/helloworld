import { PartialType } from '@nestjs/swagger';
import { CreateTestRequest } from './create-test.request';

export class TestUpdateRequest extends PartialType(CreateTestRequest) {
  constructor(title?: string, content?: string) {
    super();
    this.title = title;
    this.content = content;
  }
}
