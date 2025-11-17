import { Module } from '@nestjs/common';
import { PrismaModule } from './prisma/prisma.module';
import { TestModule } from './test/test.module';

@Module({
  imports: [PrismaModule, TestModule],
  controllers: [],
  providers: [],
})
export class AppModule {}
