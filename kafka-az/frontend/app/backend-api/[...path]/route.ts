import { proxyBackendRequest } from "@/_app/api-routes";

type ProxyRouteContext = {
  params: Promise<{ path: string[] }>;
};

async function handleProxy(request: Request, context: ProxyRouteContext): Promise<Response> {
  const { path } = await context.params;
  return proxyBackendRequest(request, path);
}

export const GET = handleProxy;
export const POST = handleProxy;
export const PUT = handleProxy;
export const PATCH = handleProxy;
export const DELETE = handleProxy;
