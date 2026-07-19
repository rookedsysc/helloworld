import { LoginPage } from "@/_pages/login";

type LoginRouteProps = {
  searchParams: Promise<{ next?: string }>;
};

export default async function LoginRoute({ searchParams }: LoginRouteProps) {
  const { next } = await searchParams;
  const nextPath = next?.startsWith("/") && !next.startsWith("//") ? next : "/posts";
  return <LoginPage nextPath={nextPath} />;
}
