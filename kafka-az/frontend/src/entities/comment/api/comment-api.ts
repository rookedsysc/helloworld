import { apiRequest } from "../../../shared/api";
import type { Comment, CommentInput } from "../model/types";

export function getComments(postId: number): Promise<Comment[]> {
  return apiRequest<Comment[]>(`/v1/posts/${postId}/comments`, { method: "GET" });
}

export function createComment(
  postId: number,
  comment: CommentInput,
  accessToken: string,
): Promise<Comment> {
  return apiRequest<Comment>(
    `/v1/posts/${postId}/comments`,
    { method: "POST", body: JSON.stringify(comment) },
    accessToken,
  );
}
