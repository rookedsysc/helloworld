"use client";

import { useState } from "react";

import { deletePost } from "../../../entities/post";
import { messageFromError } from "../../../shared/api";

type DeletePostButtonProps = {
  postId: number;
  accessToken: string;
  onDeleted: () => void;
};

export function DeletePostButton({ postId, accessToken, onDeleted }: DeletePostButtonProps) {
  const [errorMessage, setErrorMessage] = useState<string | null>(null);
  const [isSubmitting, setIsSubmitting] = useState(false);

  async function handleDelete() {
    if (!window.confirm("이 게시글을 삭제할까요?")) {
      return;
    }
    setIsSubmitting(true);
    setErrorMessage(null);
    try {
      await deletePost(postId, accessToken);
      onDeleted();
    } catch (error) {
      setErrorMessage(messageFromError(error));
      setIsSubmitting(false);
    }
  }

  return (
    <div className="delete-action">
      <button className="danger-button" type="button" disabled={isSubmitting} onClick={() => void handleDelete()}>
        {isSubmitting ? "삭제 중…" : "삭제"}
      </button>
      {errorMessage && <span className="inline-error" role="alert">{errorMessage}</span>}
    </div>
  );
}
