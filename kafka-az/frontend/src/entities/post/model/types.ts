export type Post = {
  id: number;
  memberId: number;
  title: string;
  content: string;
};

export type PostInput = {
  title: string;
  content: string;
};
