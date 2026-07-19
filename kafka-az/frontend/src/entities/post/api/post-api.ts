import { apiRequest } from "../../../shared/api";
import type { Post, PostInput } from "../model/types";

export function getPosts(): Promise<Post[]> {
  return apiRequest<Post[]>("/v1/posts", { method: "GET" });
}

export function getPost(postId: number): Promise<Post> {
  return apiRequest<Post>(`/v1/posts/${postId}`, { method: "GET" });
}

export function createPost(post: PostInput, accessToken: string): Promise<Post> {
  return apiRequest<Post>(
    "/v1/posts",
    { method: "POST", body: JSON.stringify(post) },
    accessToken,
  );
}

export function updatePost(
  postId: number,
  post: PostInput,
  accessToken: string,
): Promise<Post> {
  return apiRequest<Post>(
    `/v1/posts/${postId}`,
    { method: "PUT", body: JSON.stringify(post) },
    accessToken,
  );
}

export function deletePost(postId: number, accessToken: string): Promise<void> {
  return apiRequest<void>(`/v1/posts/${postId}`, { method: "DELETE" }, accessToken);
}
