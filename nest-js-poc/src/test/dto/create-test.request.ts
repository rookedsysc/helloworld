import { ApiProperty } from '@nestjs/swagger';
import { IsNotEmpty } from 'class-validator';

export class CreateTestRequest {
  @ApiProperty({
    description: '테스트 제목',
    example: 'Test Title',
  })
  @IsNotEmpty()
  title: string;

  @ApiProperty({
    description: '테스트 내용',
    example: 'Test Content',
  })
  @IsNotEmpty()
  content: string;

  constructor(title: string, content: string) {
    this.title = title;
    this.content = content;
  }
}
