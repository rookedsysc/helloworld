import { PostDetailPage } from "@/_pages/post-detail";

type PostDetailRouteProps = {
  params: Promise<{ postId: string }>;
};

export default async function PostDetailRoute({ params }: PostDetailRouteProps) {
  const { postId } = await params;
  return <PostDetailPage postId={Number(postId)} />;
}
