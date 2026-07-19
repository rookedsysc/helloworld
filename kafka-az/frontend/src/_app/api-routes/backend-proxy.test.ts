import { afterEach, describe, expect, it, vi } from "vitest";

import { proxyBackendRequest } from "./backend-proxy";

describe("backend runtime proxy", () => {
  afterEach(() => {
    vi.restoreAllMocks();
    delete process.env.BACKEND_API_URL;
  });

  it("forwards the path, query, method, headers, and body to the configured backend", async () => {
    process.env.BACKEND_API_URL = "http://backend:8080/api";
    const fetchMock = vi.spyOn(globalThis, "fetch").mockResolvedValue(
      new Response(JSON.stringify([{ id: 1 }]), {
        status: 200,
        headers: { "Content-Type": "application/json" },
      }),
    );
    const request = new Request("http://frontend/backend-api/v1/posts?size=20", {
      method: "POST",
      headers: {
        Authorization: "Bearer access-token",
        "Content-Type": "application/json",
      },
      body: JSON.stringify({ title: "Runtime proxy" }),
    });

    const response = await proxyBackendRequest(request, ["v1", "posts"]);

    expect(response.status).toBe(200);
    expect(await response.json()).toEqual([{ id: 1 }]);
    expect(fetchMock).toHaveBeenCalledWith(
      "http://backend:8080/api/v1/posts?size=20",
      expect.objectContaining({
        method: "POST",
        body: JSON.stringify({ title: "Runtime proxy" }),
      }),
    );
    const forwardedHeaders = new Headers(fetchMock.mock.calls[0][1]?.headers);
    expect(forwardedHeaders.get("Authorization")).toBe("Bearer access-token");
  });

  it("returns a diagnostic 502 response when the backend cannot be reached", async () => {
    vi.spyOn(globalThis, "fetch").mockRejectedValue(new TypeError("fetch failed"));

    const response = await proxyBackendRequest(
      new Request("http://frontend/backend-api/v1/posts"),
      ["v1", "posts"],
    );

    expect(response.status).toBe(502);
    await expect(response.json()).resolves.toEqual({
      status: 502,
      message: "백엔드에 연결할 수 없습니다.",
    });
  });
});
