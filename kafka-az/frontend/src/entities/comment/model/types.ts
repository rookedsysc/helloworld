export type Comment = {
  id: number;
  postId: number;
  memberId: number;
  content: string;
  createdAt: string;
};

export type CommentInput = {
  content: string;
};
