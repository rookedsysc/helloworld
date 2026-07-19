"use client";

import { FormEvent, useState } from "react";

import { createPost } from "../../../entities/post";
import type { Post } from "../../../entities/post";
import { messageFromError } from "../../../shared/api";

type CreatePostFormProps = {
  accessToken: string;
  onCreated: (post: Post) => void;
};

export function CreatePostForm({ accessToken, onCreated }: CreatePostFormProps) {
  const [draft, setDraft] = useState({ title: "", content: "" });
  const [errorMessage, setErrorMessage] = useState<string | null>(null);
  const [isSubmitting, setIsSubmitting] = useState(false);

  async function handleSubmit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    setIsSubmitting(true);
    setErrorMessage(null);
    try {
      onCreated(await createPost(draft, accessToken));
    } catch (error) {
      setErrorMessage(messageFromError(error));
    } finally {
      setIsSubmitting(false);
    }
  }

  return (
    <form className="stack-form" onSubmit={handleSubmit}>
      {errorMessage && <p className="form-error" role="alert">{errorMessage}</p>}
      <label>
        게시글 제목
        <input
          value={draft.title}
          maxLength={100}
          required
          placeholder="게시글 제목을 입력하세요."
          onChange={(event) => setDraft({ ...draft, title: event.target.value })}
        />
      </label>
      <label>
        게시글 본문
        <textarea
          value={draft.content}
          maxLength={5000}
          required
          placeholder="게시글 내용을 입력하세요."
          onChange={(event) => setDraft({ ...draft, content: event.target.value })}
        />
      </label>
      <button className="primary-button" type="submit" disabled={isSubmitting}>
        {isSubmitting ? "저장 중…" : "게시글 등록"}
      </button>
    </form>
  );
}
