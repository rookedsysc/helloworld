"use client";

import { FormEvent, useState } from "react";

import { createComment } from "../../../entities/comment";
import type { Comment } from "../../../entities/comment";
import { messageFromError } from "../../../shared/api";

type CommentFormProps = {
  postId: number;
  accessToken: string;
  onCreated: (comment: Comment) => void;
};

export function CommentForm({ postId, accessToken, onCreated }: CommentFormProps) {
  const [content, setContent] = useState("");
  const [errorMessage, setErrorMessage] = useState<string | null>(null);
  const [isSubmitting, setIsSubmitting] = useState(false);

  async function handleSubmit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    setIsSubmitting(true);
    setErrorMessage(null);
    try {
      onCreated(await createComment(postId, { content }, accessToken));
      setContent("");
    } catch (error) {
      setErrorMessage(messageFromError(error));
    } finally {
      setIsSubmitting(false);
    }
  }

  return (
    <form className="comment-form" onSubmit={handleSubmit}>
      {errorMessage && <p className="form-error" role="alert">{errorMessage}</p>}
      <label>댓글 내용<textarea value={content} maxLength={1000} required placeholder="댓글을 입력하세요." onChange={(event) => setContent(event.target.value)} /></label>
      <button className="primary-button" type="submit" disabled={isSubmitting}>{isSubmitting ? "등록 중…" : "댓글 등록"}</button>
    </form>
  );
}
