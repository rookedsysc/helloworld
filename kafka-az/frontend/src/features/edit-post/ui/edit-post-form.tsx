"use client";

import { FormEvent, useState } from "react";

import { updatePost } from "../../../entities/post";
import type { Post } from "../../../entities/post";
import { messageFromError } from "../../../shared/api";

type EditPostFormProps = {
  post: Post;
  accessToken: string;
  onUpdated: (post: Post) => void;
};

export function EditPostForm({ post, accessToken, onUpdated }: EditPostFormProps) {
  const [draft, setDraft] = useState({ title: post.title, content: post.content });
  const [errorMessage, setErrorMessage] = useState<string | null>(null);
  const [isSubmitting, setIsSubmitting] = useState(false);

  async function handleSubmit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    setIsSubmitting(true);
    setErrorMessage(null);
    try {
      onUpdated(await updatePost(post.id, draft, accessToken));
    } catch (error) {
      setErrorMessage(messageFromError(error));
    } finally {
      setIsSubmitting(false);
    }
  }

  return (
    <form className="stack-form compact-editor" onSubmit={handleSubmit}>
      <h3>게시글 수정</h3>
      {errorMessage && <p className="form-error" role="alert">{errorMessage}</p>}
      <label>게시글 제목<input value={draft.title} maxLength={100} required onChange={(event) => setDraft({ ...draft, title: event.target.value })} /></label>
      <label>게시글 본문<textarea value={draft.content} maxLength={5000} required onChange={(event) => setDraft({ ...draft, content: event.target.value })} /></label>
      <button className="secondary-button" type="submit" disabled={isSubmitting}>{isSubmitting ? "수정 중…" : "게시글 수정"}</button>
    </form>
  );
}
