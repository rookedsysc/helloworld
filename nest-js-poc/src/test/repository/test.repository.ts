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

  async findById({ id }: { id: number }): Promise<Test | null> {
    return await this.prisma.test.findUnique({
      where: { id },
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

  async update({
    id,
    title,
    content,
  }: {
    id: number;
    title: string | null | undefined;
    content: string | null | undefined;
  }): Promise<void> {
    await this.prisma.test.update({
      where: {
        id: id,
      },
      data: {
        title: title ?? undefined,
        content: content ?? undefined,
      },
    });
  }
}
