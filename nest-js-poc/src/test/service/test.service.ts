import { Injectable, NotFoundException } from '@nestjs/common';
import { TestRepository } from '../repository/test.repository';
import { CreateTestRequest } from '../dto/create-test.request';
import { CreateTestResponse } from '../dto/create-test.response';
import { PageRequest } from 'src/common/model/page.request';
import { TestListResponse } from '../dto/test-list.response';
import { Test } from 'generated/prisma';
import { TestDetailResponse } from '../dto/test-detail.response';

@Injectable()
export class TestService {
  constructor(private readonly testRepository: TestRepository) {}

  async getTestList(request: PageRequest): Promise<TestListResponse[]> {
    const tests = await this.testRepository.findAll({
      page: request.currentPage,
      size: request.pageSize,
    });
    return tests.map(
      (test) =>
        new TestListResponse({
          id: test.id,
          title: test.title,
        }),
    );
  }

  async getTestById(id: number): Promise<TestDetailResponse> {
    const test: Test = await this.testRepository
      .findById({ id })
      .then((res) => {
        if (!res) {
          throw new NotFoundException(`Test with ID ${id} not found`);
        }
        return res;
      });

    return new TestDetailResponse({
      id: test.id,
      title: test.title,
      content: test.content,
    });
  }

  async createTest(request: CreateTestRequest): Promise<CreateTestResponse> {
    const testEntity = await this.testRepository.create(
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
