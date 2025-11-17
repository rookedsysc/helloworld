import { ApiProperty } from '@nestjs/swagger';

export class TestDetailResponse {
  @ApiProperty({
    description: '테스트 ID',
    example: 1,
  })
  id: number;

  @ApiProperty({
    description: '테스트 제목',
    example: 'Test Title',
  })
  title: string;

  @ApiProperty({
    description: '테스트 내용',
    example: 'Test Content',
  })
  content: string;

  constructor({
    id,
    title,
    content,
  }: {
    id: number;
    title: string;
    content: string;
  }) {
    this.id = id;
    this.title = title;
    this.content = content;
  }
}
