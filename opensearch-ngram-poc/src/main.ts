import { ValidationPipe } from '@nestjs/common';
import { NestFactory } from '@nestjs/core';
import { FastifyAdapter, NestFastifyApplication } from '@nestjs/platform-fastify';
import { DocumentBuilder, SwaggerModule } from '@nestjs/swagger';

import { AppModule } from './app.module';
import { HTTP_PORT } from './connection-settings';

/**
 * 검색 API를 기동한다.
 *
 * 측정 대상은 검색 엔진이지 HTTP 프레임워크가 아니므로, 앱 레이어 오버헤드가
 * 한 자릿수 밀리초 응답을 오염시키지 않도록 Fastify 어댑터를 쓴다.
 */
async function bootstrap(): Promise<void> {
  const app = await NestFactory.create<NestFastifyApplication>(AppModule, new FastifyAdapter());
  app.useGlobalPipes(new ValidationPipe({ transform: true, whitelist: true }));

  const swaggerConfig = new DocumentBuilder()
    .setTitle('OpenSearch ngram PoC')
    .setDescription('MySQL ngram(1) FULLTEXT와 OpenSearch 1-gram의 1글자 검색 성능 비교')
    .setVersion('1.0')
    .build();
  SwaggerModule.setup('api-docs', app, SwaggerModule.createDocument(app, swaggerConfig));

  await app.listen({ port: HTTP_PORT, host: '0.0.0.0' });
}

void bootstrap();
