const DEFAULT_BACKEND_API_URL = "http://localhost:8080/api";
const BODYLESS_METHODS = new Set(["GET", "HEAD"]);

export async function proxyBackendRequest(
  request: Request,
  pathSegments: string[],
): Promise<Response> {
  const backendApiUrl = (process.env.BACKEND_API_URL ?? DEFAULT_BACKEND_API_URL).replace(/\/$/, "");
  const sourceUrl = new URL(request.url);
  const targetUrl = `${backendApiUrl}/${pathSegments.map(encodeURIComponent).join("/")}${sourceUrl.search}`;
  const headers = new Headers(request.headers);
  headers.delete("host");
  headers.delete("content-length");

  try {
    const body = BODYLESS_METHODS.has(request.method) ? undefined : await request.text();
    const backendResponse = await fetch(targetUrl, {
      method: request.method,
      headers,
      body,
      redirect: "manual",
      cache: "no-store",
    });
    const responseHeaders = new Headers(backendResponse.headers);
    responseHeaders.delete("content-length");
    responseHeaders.delete("content-encoding");
    responseHeaders.delete("transfer-encoding");
    return new Response(backendResponse.body, {
      status: backendResponse.status,
      statusText: backendResponse.statusText,
      headers: responseHeaders,
    });
  } catch {
    return Response.json(
      { status: 502, message: "백엔드에 연결할 수 없습니다." },
      { status: 502 },
    );
  }
}
