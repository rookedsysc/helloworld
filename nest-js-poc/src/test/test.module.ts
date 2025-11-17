import { Module } from '@nestjs/common';
import { TestService } from './service/test.service';
import { TestController } from './controller/test.controller';
import { TestRepository } from './repository/test.repository';
import { PrismaService } from 'src/prisma/prisma.service';

@Module({
  controllers: [TestController],
  providers: [TestService, TestRepository, PrismaService],
})
export class TestModule {}
