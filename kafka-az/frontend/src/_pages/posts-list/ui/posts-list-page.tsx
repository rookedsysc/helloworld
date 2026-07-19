"use client";

import Link from "next/link";
import { useCallback, useEffect, useState } from "react";

import { getPosts, PostCard } from "../../../entities/post";
import type { Post } from "../../../entities/post";
import { messageFromError } from "../../../shared/api";
import { routes } from "../../../shared/routes";
import { ErrorNotice } from "../../../shared/ui";
import { SiteHeader } from "../../../widgets/site-header";

export function PostsListPage() {
  const [posts, setPosts] = useState<Post[]>([]);
  const [isLoading, setIsLoading] = useState(true);
  const [errorMessage, setErrorMessage] = useState<string | null>(null);

  const loadPosts = useCallback(async () => {
    setIsLoading(true);
    setErrorMessage(null);
    try {
      setPosts(await getPosts());
    } catch (error) {
      setPosts([]);
      setErrorMessage(messageFromError(error));
    } finally {
      setIsLoading(false);
    }
  }, []);

  useEffect(() => {
    let isActive = true;
    getPosts()
      .then((loadedPosts) => {
        if (isActive) {
          setPosts(loadedPosts);
        }
      })
      .catch((error: unknown) => {
        if (isActive) {
          setPosts([]);
          setErrorMessage(messageFromError(error));
        }
      })
      .finally(() => {
        if (isActive) {
          setIsLoading(false);
        }
      });
    return () => {
      isActive = false;
    };
  }, []);

  return (
    <div className="page-shell">
      <SiteHeader />
      <main>
        <section className="page-hero">
          <div>
            <p className="eyebrow">POST STREAM · BACKEND API</p>
            <h1>게시물 목록</h1>
            <p>Spring backend가 반환한 게시물을 확인하고 상세 페이지로 이동합니다.</p>
          </div>
          <Link className="primary-link" href={routes.createPost}>새 게시물 작성</Link>
        </section>

        {isLoading && <p className="state-card" role="status">게시글을 불러오는 중…</p>}
        {!isLoading && errorMessage && <ErrorNotice message={errorMessage} onRetry={() => void loadPosts()} />}
        {!isLoading && !errorMessage && posts.length === 0 && (
          <section className="state-card empty-state">
            <strong>아직 게시글이 없습니다.</strong>
            <span>로그인하면 첫 게시글을 작성할 수 있어요.</span>
          </section>
        )}
        {!isLoading && !errorMessage && posts.length > 0 && (
          <section className="post-grid" aria-label="게시물 목록">
            {posts.map((post) => <PostCard key={post.id} post={post} />)}
          </section>
        )}
      </main>
    </div>
  );
}
