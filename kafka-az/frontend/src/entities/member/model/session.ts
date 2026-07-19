"use client";

import { useSyncExternalStore } from "react";

const SESSION_TOKEN_KEY = "kafka-az-access-token";
const SESSION_LOGIN_ID_KEY = "kafka-az-login-id";
const SESSION_AUTH_EVENT = "kafka-az-session-auth-change";
const EMPTY_SESSION_SNAPSHOT = "\n";

export type Session = {
  accessToken: string | null;
  loginId: string;
  memberId: number | null;
};

export function useSession(): Session {
  const snapshot = useSyncExternalStore(
    subscribeToSession,
    getSessionSnapshot,
    getServerSessionSnapshot,
  );
  return parseSessionSnapshot(snapshot);
}

export function readSession(): Session {
  return parseSessionSnapshot(getSessionSnapshot());
}

export function saveSession(accessToken: string, loginId: string): void {
  sessionStorage.setItem(SESSION_TOKEN_KEY, accessToken);
  sessionStorage.setItem(SESSION_LOGIN_ID_KEY, loginId);
  window.dispatchEvent(new Event(SESSION_AUTH_EVENT));
}

export function clearSession(): void {
  sessionStorage.removeItem(SESSION_TOKEN_KEY);
  sessionStorage.removeItem(SESSION_LOGIN_ID_KEY);
  window.dispatchEvent(new Event(SESSION_AUTH_EVENT));
}

function subscribeToSession(onStoreChange: () => void): () => void {
  window.addEventListener(SESSION_AUTH_EVENT, onStoreChange);
  window.addEventListener("storage", onStoreChange);
  return () => {
    window.removeEventListener(SESSION_AUTH_EVENT, onStoreChange);
    window.removeEventListener("storage", onStoreChange);
  };
}

function getSessionSnapshot(): string {
  const accessToken = sessionStorage.getItem(SESSION_TOKEN_KEY) ?? "";
  const loginId = sessionStorage.getItem(SESSION_LOGIN_ID_KEY) ?? "";
  return `${accessToken}\n${loginId}`;
}

function getServerSessionSnapshot(): string {
  return EMPTY_SESSION_SNAPSHOT;
}

function parseSessionSnapshot(snapshot: string): Session {
  const [storedAccessToken, loginId] = snapshot.split("\n");
  const accessToken = storedAccessToken || null;
  return {
    accessToken,
    loginId,
    memberId: memberIdFromToken(accessToken),
  };
}

function memberIdFromToken(accessToken: string | null): number | null {
  if (!accessToken) {
    return null;
  }
  try {
    const encodedPayload = accessToken.split(".")[1];
    const payload = JSON.parse(atob(encodedPayload)) as { sub?: string };
    const memberId = Number(payload.sub);
    return Number.isFinite(memberId) ? memberId : null;
  } catch {
    return null;
  }
}
