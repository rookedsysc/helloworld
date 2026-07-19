import Link from "next/link";

import { routes } from "../../../shared/routes";
import type { Post } from "../model/types";

export function PostCard({ post }: { post: Post }) {
  return (
    <Link className="post-card" href={routes.postDetail(post.id)}>
      <span className="post-id">#{String(post.id).padStart(3, "0")}</span>
      <strong>{post.title}</strong>
      <span>{post.content}</span>
      <small>Member {post.memberId}</small>
    </Link>
  );
}
