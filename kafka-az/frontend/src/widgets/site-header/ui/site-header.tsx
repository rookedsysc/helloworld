"use client";

import Link from "next/link";
import { useRouter } from "next/navigation";

import { clearSession, useSession } from "../../../entities/member";
import { routes } from "../../../shared/routes";

export function SiteHeader() {
  const router = useRouter();
  const session = useSession();

  function handleLogout() {
    clearSession();
    router.push(routes.posts);
  }

  return (
    <header className="site-header">
      <Link className="brand" href={routes.posts}>
        <span>KAFKA AZ</span>
        <strong>Post workspace</strong>
      </Link>
      <nav aria-label="주요 메뉴">
        <Link href={routes.posts}>게시물 목록</Link>
        <Link href={routes.createPost}>게시물 작성</Link>
        {session.accessToken ? (
          <button type="button" onClick={handleLogout}>{session.loginId || "로그아웃"}</button>
        ) : (
          <Link href={routes.login}>로그인</Link>
        )}
      </nav>
    </header>
  );
}
