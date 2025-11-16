import { Injectable } from '@nestjs/common';
import { Test } from 'generated/prisma';
import { PrismaService } from 'src/prisma/prisma.service';

@Injectable()
export class TestRepository {
  constructor(private readonly prisma: PrismaService) {}

  async createTest(title: string, content: string): Promise<Test> {
    return await this.prisma.test.create({
      data: {
        title,
        content,
      },
    });
  }
}
