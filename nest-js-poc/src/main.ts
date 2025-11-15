import { NestFactory } from '@nestjs/core';
import { AppModule } from './app.module';
import { DocumentBuilder } from '@nestjs/swagger/dist/document-builder';
import { SwaggerModule } from '@nestjs/swagger';

async function bootstrap() {
  const app = await NestFactory.create(AppModule);

  // Swagger Setup //
  const config = new DocumentBuilder()
    .setTitle('NestJS POC')
    .setDescription('입사 전 Nest POC 프로젝트 API 문서')
    .setVersion('v1')
    .build();
  const documents = SwaggerModule.createDocument(app, config);
  SwaggerModule.setup('swagger-ui/index.html', app, documents, {
    swaggerOptions: {
      filter: true,
      displayRequestDuration: true,
    },
  });

  await app.listen(process.env.PORT ?? 3000);
}
bootstrap();
