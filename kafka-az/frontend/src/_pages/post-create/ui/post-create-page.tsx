"use client";

import Link from "next/link";
import { useRouter } from "next/navigation";

import { useSession } from "../../../entities/member";
import { CreatePostForm } from "../../../features/create-post";
import { routes } from "../../../shared/routes";
import { SiteHeader } from "../../../widgets/site-header";

export function PostCreatePage() {
  const router = useRouter();
  const session = useSession();

  return (
    <div className="page-shell">
      <SiteHeader />
      <main className="narrow-page">
        <section className="page-hero compact-hero">
          <div>
            <p className="eyebrow">NEW POST · AUTHENTICATED</p>
            <h1>게시물 작성</h1>
            <p>제목과 본문을 입력하면 backend 게시물 API에 바로 저장됩니다.</p>
          </div>
        </section>
        {!session.accessToken ? (
          <section className="state-card auth-required">
            <strong>로그인이 필요합니다.</strong>
            <span>게시물을 작성하려면 작업 세션을 먼저 시작해 주세요.</span>
            <Link className="primary-link" href={`${routes.login}?next=${encodeURIComponent(routes.createPost)}`}>로그인 페이지로 이동</Link>
          </section>
        ) : (
          <section className="form-panel" aria-label="게시물 작성 양식">
            <CreatePostForm
              accessToken={session.accessToken}
              onCreated={(post) => router.push(routes.postDetail(post.id))}
            />
          </section>
        )}
      </main>
    </div>
  );
}
