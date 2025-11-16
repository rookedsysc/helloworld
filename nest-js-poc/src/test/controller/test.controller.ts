import { Body, Controller, Get, Post, Query } from '@nestjs/common';
import { ApiOperation, ApiTags } from '@nestjs/swagger';
import { TestService } from '../service/test.service';
import { CreateTestRequest } from '../dto/create-test.request';
import { CreateTestResponse } from '../dto/create-test.response';
import { PageRequest } from 'src/common/model/page.request';
import { TestListResponse } from '../dto/test-list.response';

@ApiTags('prisma 학습용 테스트 컨트롤러')
@Controller('open-api/test')
export class TestController {
  constructor(private readonly testService: TestService) {}

  @ApiOperation({ summary: '테스트 목록 조회' })
  @Get()
  async getTestList(
    @Query() request: PageRequest,
  ): Promise<TestListResponse[]> {
    return await this.testService.getTestList(request);
  }

  @ApiOperation({ summary: '테스트 데이터 생성' })
  @Post()
  async createTest(
    @Body() request: CreateTestRequest,
  ): Promise<CreateTestResponse> {
    return await this.testService.createTest(request);
  }
}
