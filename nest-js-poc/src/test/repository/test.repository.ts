import { Injectable } from '@nestjs/common';
import { Test } from 'generated/prisma';
import { Page } from 'src/common/model/page.response';
import { PrismaService } from 'src/prisma/prisma.service';

@Injectable()
export class TestRepository {
  constructor(private readonly prisma: PrismaService) {}

  async findAll({
    page,
    size,
  }: {
    page: number;
    size: number;
  }): Promise<Test[]> {
    return await this.prisma.test.findMany({
      skip: (page - 1) * size,
      take: size,
    });
  }

  async create(title: string, content: string): Promise<Test> {
    return await this.prisma.test.create({
      data: {
        title,
        content,
      },
    });
  }
}
