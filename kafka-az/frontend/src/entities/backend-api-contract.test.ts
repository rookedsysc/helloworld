import { afterEach, describe, expect, it, vi } from "vitest";

import { createComment, getComments } from "./comment";
import { createPost, deletePost, getPost, getPosts, updatePost } from "./post";
import { login } from "../features/auth";

const ACCESS_TOKEN = "access-token";

function jsonResponse(body: unknown, status = 200): Response {
  return new Response(JSON.stringify(body), {
    status,
    headers: { "Content-Type": "application/json" },
  });
}

describe("backend API contract", () => {
  afterEach(() => {
    vi.restoreAllMocks();
  });

  it("preserves auth, post, and comment endpoint contracts", async () => {
    const fetchMock = vi.spyOn(globalThis, "fetch")
      .mockResolvedValueOnce(jsonResponse({ accessToken: ACCESS_TOKEN, tokenType: "Bearer", isNewMember: true }))
      .mockResolvedValueOnce(jsonResponse([]))
      .mockResolvedValueOnce(jsonResponse({ id: 1, memberId: 7, title: "Title", content: "Body" }))
      .mockResolvedValueOnce(jsonResponse({ id: 1, memberId: 7, title: "Title", content: "Body" }, 201))
      .mockResolvedValueOnce(jsonResponse({ id: 1, memberId: 7, title: "Changed", content: "Body" }))
      .mockResolvedValueOnce(jsonResponse([]))
      .mockResolvedValueOnce(jsonResponse({ id: 3, postId: 1, memberId: 7, content: "Comment", createdAt: "2026-07-18T00:00:00Z" }, 201))
      .mockResolvedValueOnce(new Response(null, { status: 204 }));

    await login({ id: "roky", password: "password123" });
    await getPosts();
    await getPost(1);
    await createPost({ title: "Title", content: "Body" }, ACCESS_TOKEN);
    await updatePost(1, { title: "Changed", content: "Body" }, ACCESS_TOKEN);
    await getComments(1);
    await createComment(1, { content: "Comment" }, ACCESS_TOKEN);
    await deletePost(1, ACCESS_TOKEN);

    expect(fetchMock.mock.calls.map(([path, init]) => [path, init?.method])).toEqual([
      ["/backend-api/v1/auth/login", "POST"],
      ["/backend-api/v1/posts", "GET"],
      ["/backend-api/v1/posts/1", "GET"],
      ["/backend-api/v1/posts", "POST"],
      ["/backend-api/v1/posts/1", "PUT"],
      ["/backend-api/v1/posts/1/comments", "GET"],
      ["/backend-api/v1/posts/1/comments", "POST"],
      ["/backend-api/v1/posts/1", "DELETE"],
    ]);

    for (const callIndex of [3, 4, 6, 7]) {
      const headers = new Headers(fetchMock.mock.calls[callIndex][1]?.headers);
      expect(headers.get("Authorization")).toBe(`Bearer ${ACCESS_TOKEN}`);
    }
  });
});
