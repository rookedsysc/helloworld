export class TestDetailResponse {
  id: number;
  title: string;
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
