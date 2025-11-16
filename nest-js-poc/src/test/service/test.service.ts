import { Injectable } from '@nestjs/common';
import { TestRepository } from '../repository/test.repository';
import { CreateTestRequest } from '../dto/create-test.request';
import { CreateTestResponse } from '../dto/create-test.response';

@Injectable()
export class TestService {
  constructor(private readonly testRepository: TestRepository) {}

  async createTest(request: CreateTestRequest): Promise<CreateTestResponse> {
    const testEntity = await this.testRepository.createTest(
      request.title,
      request.content,
    );
    return new CreateTestResponse({
      id: testEntity.id,
      title: testEntity.title,
      content: testEntity.content,
    });
  }
}
