import { render, screen } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import { beforeEach, describe, expect, it, vi } from "vitest";

import * as authApi from "../features/auth/api/login";
import * as commentApi from "../entities/comment/api/comment-api";
import * as postApi from "../entities/post/api/post-api";
import { LoginPage } from "./login";
import { PostCreatePage } from "./post-create";
import { PostDetailPage } from "./post-detail";
import { PostsListPage } from "./posts-list";

const push = vi.fn();

vi.mock("next/navigation", () => ({
  useRouter: () => ({ push, replace: vi.fn(), refresh: vi.fn() }),
  useSearchParams: () => new URLSearchParams(),
}));

vi.mock("../features/auth/api/login", () => ({ login: vi.fn() }));
vi.mock("../entities/post/api/post-api", () => ({
  createPost: vi.fn(),
  deletePost: vi.fn(),
  getPost: vi.fn(),
  getPosts: vi.fn(),
  updatePost: vi.fn(),
}));
vi.mock("../entities/comment/api/comment-api", () => ({
  createComment: vi.fn(),
  getComments: vi.fn(),
}));

describe("separated FSD pages", () => {
  beforeEach(() => {
    sessionStorage.clear();
    push.mockReset();
    vi.mocked(authApi.login).mockReset();
    vi.mocked(postApi.getPosts).mockReset();
    vi.mocked(postApi.getPost).mockReset();
    vi.mocked(postApi.createPost).mockReset();
    vi.mocked(commentApi.getComments).mockReset();
    vi.mocked(commentApi.getComments).mockResolvedValue([]);
  });

  it("renders API posts as links on the list page", async () => {
    vi.mocked(postApi.getPosts).mockResolvedValue([
      { id: 4, memberId: 7, title: "API 게시물", content: "backend response" },
    ]);

    render(<PostsListPage />);

    expect(await screen.findByRole("link", { name: /API 게시물/ })).toHaveAttribute(
      "href",
      "/posts/4",
    );
  });

  it("keeps a backend failure distinct from the empty list and supports retry", async () => {
    const user = userEvent.setup();
    vi.mocked(postApi.getPosts)
      .mockRejectedValueOnce(new Error("백엔드에 연결할 수 없습니다."))
      .mockResolvedValueOnce([]);

    render(<PostsListPage />);

    expect(await screen.findByRole("alert")).toHaveTextContent("백엔드에 연결할 수 없습니다.");
    expect(screen.queryByText("아직 게시글이 없습니다.")).not.toBeInTheDocument();
    await user.click(screen.getByRole("button", { name: "다시 불러오기" }));
    expect(await screen.findByText("아직 게시글이 없습니다.")).toBeInTheDocument();
  });

  it("stores the login session and navigates to the posts page", async () => {
    const user = userEvent.setup();
    vi.mocked(authApi.login).mockResolvedValue({
      accessToken: "header.eyJzdWIiOiI3In0.signature",
      tokenType: "Bearer",
      isNewMember: false,
    });

    render(<LoginPage />);
    await user.type(screen.getByLabelText("로그인 ID"), "roky");
    await user.type(screen.getByLabelText("비밀번호"), "password123");
    await user.click(screen.getByRole("button", { name: "로그인 / 가입" }));

    expect(sessionStorage.getItem("kafka-az-access-token")).toContain("eyJzdWIiOiI3In0");
    expect(push).toHaveBeenCalledWith("/posts");
  });

  it("creates a post on its own page and navigates to the detail route", async () => {
    const user = userEvent.setup();
    sessionStorage.setItem("kafka-az-access-token", "access-token");
    vi.mocked(postApi.createPost).mockResolvedValue({
      id: 12,
      memberId: 7,
      title: "분리된 작성 페이지",
      content: "FSD",
    });

    render(<PostCreatePage />);
    await user.type(screen.getByLabelText("게시글 제목"), "분리된 작성 페이지");
    await user.type(screen.getByLabelText("게시글 본문"), "FSD");
    await user.click(screen.getByRole("button", { name: "게시글 등록" }));

    expect(postApi.createPost).toHaveBeenCalledWith(
      { title: "분리된 작성 페이지", content: "FSD" },
      "access-token",
    );
    expect(push).toHaveBeenCalledWith("/posts/12");
  });

  it("loads a post and its comments on the detail page", async () => {
    vi.mocked(postApi.getPost).mockResolvedValue({
      id: 4,
      memberId: 7,
      title: "상세 게시물",
      content: "상세 본문",
    });
    vi.mocked(commentApi.getComments).mockResolvedValue([
      { id: 1, postId: 4, memberId: 8, content: "댓글", createdAt: "2026-07-18T00:00:00Z" },
    ]);

    render(<PostDetailPage postId={4} />);

    expect(await screen.findByRole("heading", { name: "상세 게시물" })).toBeInTheDocument();
    expect(screen.getByText("상세 본문")).toBeInTheDocument();
    expect(screen.getByText("댓글", { selector: ".comment p" })).toBeInTheDocument();
  });
});
