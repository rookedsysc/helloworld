"use client";

import Link from "next/link";
import { useRouter } from "next/navigation";

import { LoginForm } from "../../../features/auth";
import { routes } from "../../../shared/routes";

export function LoginPage({ nextPath = routes.posts }: { nextPath?: string }) {
  const router = useRouter();

  return (
    <main className="auth-page page-shell">
      <section className="auth-copy">
        <p className="eyebrow">KAFKA AZ · IDENTITY</p>
        <h1>Backend와 연결할<br />작업 세션을 시작하세요.</h1>
        <p>처음 사용하는 ID는 자동으로 가입되며, 로그인 세션은 현재 브라우저 탭에만 유지됩니다.</p>
        <Link className="text-link" href={routes.posts}>로그인 없이 게시물 둘러보기 →</Link>
      </section>
      <section className="form-panel" aria-labelledby="login-heading">
        <span className="panel-index">LOGIN</span>
        <h2 id="login-heading">로그인 / 가입</h2>
        <LoginForm onAuthenticated={() => router.push(nextPath)} />
      </section>
    </main>
  );
}
