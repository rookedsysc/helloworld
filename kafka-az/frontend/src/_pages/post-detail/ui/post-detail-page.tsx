"use client";

import Link from "next/link";
import { useCallback, useEffect, useState } from "react";
import { useRouter } from "next/navigation";

import { getComments } from "../../../entities/comment";
import type { Comment } from "../../../entities/comment";
import { useSession } from "../../../entities/member";
import { getPost } from "../../../entities/post";
import type { Post } from "../../../entities/post";
import { CommentForm } from "../../../features/create-comment";
import { DeletePostButton } from "../../../features/delete-post";
import { EditPostForm } from "../../../features/edit-post";
import { messageFromError } from "../../../shared/api";
import { routes } from "../../../shared/routes";
import { ErrorNotice } from "../../../shared/ui";
import { SiteHeader } from "../../../widgets/site-header";

export function PostDetailPage({ postId }: { postId: number }) {
  const router = useRouter();
  const session = useSession();
  const [post, setPost] = useState<Post | null>(null);
  const [comments, setComments] = useState<Comment[]>([]);
  const [isLoading, setIsLoading] = useState(true);
  const [errorMessage, setErrorMessage] = useState<string | null>(null);

  const loadDetail = useCallback(async () => {
    setIsLoading(true);
    setErrorMessage(null);
    try {
      const [loadedPost, loadedComments] = await Promise.all([
        getPost(postId),
        getComments(postId),
      ]);
      setPost(loadedPost);
      setComments(loadedComments);
    } catch (error) {
      setPost(null);
      setComments([]);
      setErrorMessage(messageFromError(error));
    } finally {
      setIsLoading(false);
    }
  }, [postId]);

  useEffect(() => {
    let isActive = true;
    Promise.all([getPost(postId), getComments(postId)])
      .then(([loadedPost, loadedComments]) => {
        if (isActive) {
          setPost(loadedPost);
          setComments(loadedComments);
        }
      })
      .catch((error: unknown) => {
        if (isActive) {
          setPost(null);
          setComments([]);
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
  }, [postId]);

  const isOwner = Boolean(post && session.memberId === post.memberId);

  return (
    <div className="page-shell">
      <SiteHeader />
      <main className="detail-page">
        {isLoading && <p className="state-card" role="status">게시글을 불러오는 중…</p>}
        {!isLoading && errorMessage && <ErrorNotice message={errorMessage} onRetry={() => void loadDetail()} />}
        {!isLoading && post && (
          <>
            <article className="post-detail-card">
              <div className="detail-meta">
                <Link href={routes.posts}>← 게시물 목록</Link>
                <span>POST #{String(post.id).padStart(3, "0")} · MEMBER {post.memberId}</span>
              </div>
              <div className="detail-title-row">
                <h1>{post.title}</h1>
                {isOwner && session.accessToken && (
                  <DeletePostButton postId={post.id} accessToken={session.accessToken} onDeleted={() => router.push(routes.posts)} />
                )}
              </div>
              <p className="post-content">{post.content}</p>
              {isOwner && session.accessToken && (
                <EditPostForm post={post} accessToken={session.accessToken} onUpdated={setPost} />
              )}
            </article>

            <section className="comments-card" aria-labelledby="comments-heading">
              <div className="comments-heading">
                <h2 id="comments-heading">댓글</h2>
                <span>{comments.length}</span>
              </div>
              {comments.length === 0 ? (
                <p className="comment-empty">첫 댓글을 남겨 대화를 시작하세요.</p>
              ) : (
                <div className="comment-list">
                  {comments.map((comment) => (
                    <article className="comment" key={comment.id}>
                      <div><strong>Member {comment.memberId}</strong><time dateTime={comment.createdAt}>{formatDate(comment.createdAt)}</time></div>
                      <p>{comment.content}</p>
                    </article>
                  ))}
                </div>
              )}
              {session.accessToken ? (
                <CommentForm postId={post.id} accessToken={session.accessToken} onCreated={(comment) => setComments((current) => [...current, comment])} />
              ) : (
                <p className="login-hint"><Link href={`${routes.login}?next=${encodeURIComponent(routes.postDetail(post.id))}`}>로그인</Link>하면 댓글을 작성할 수 있습니다.</p>
              )}
            </section>
          </>
        )}
      </main>
    </div>
  );
}

function formatDate(value: string): string {
  return new Intl.DateTimeFormat("ko-KR", { dateStyle: "medium", timeStyle: "short" }).format(new Date(value));
}
