"use client";

import { FormEvent, useState } from "react";

import { saveSession } from "../../../entities/member";
import { messageFromError } from "../../../shared/api";
import { login } from "../api/login";

type LoginFormProps = {
  onAuthenticated: () => void;
};

export function LoginForm({ onAuthenticated }: LoginFormProps) {
  const [credentials, setCredentials] = useState({ id: "", password: "" });
  const [errorMessage, setErrorMessage] = useState<string | null>(null);
  const [isSubmitting, setIsSubmitting] = useState(false);

  async function handleSubmit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    setIsSubmitting(true);
    setErrorMessage(null);
    try {
      const response = await login(credentials);
      saveSession(response.accessToken, credentials.id);
      onAuthenticated();
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
        로그인 ID
        <input
          value={credentials.id}
          maxLength={50}
          required
          autoComplete="username"
          onChange={(event) => setCredentials({ ...credentials, id: event.target.value })}
        />
      </label>
      <label>
        비밀번호
        <input
          type="password"
          value={credentials.password}
          minLength={8}
          maxLength={100}
          required
          autoComplete="current-password"
          onChange={(event) => setCredentials({ ...credentials, password: event.target.value })}
        />
      </label>
      <button className="primary-button" type="submit" disabled={isSubmitting}>
        {isSubmitting ? "연결 중…" : "로그인 / 가입"}
      </button>
    </form>
  );
}
