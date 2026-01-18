// Note: @sentry/nextjs doesn't support Next.js 16 yet
// Sentry instrumentation is disabled until support is available

export async function register() {
  // No-op: Sentry instrumentation disabled
}

export const onRequestError = async (
  error: { digest: string } & Error,
  request: {
    path: string;
    method: string;
    headers: Record<string, string>;
  },
  context: { routerKind: string; routeType: string; routePath: string },
) => {
  // No-op: Sentry error capture disabled
  // Log to console in development for debugging
  if (process.env.NODE_ENV === 'development') {
    console.error('[Request Error]', {
      error: error.message,
      digest: error.digest,
      path: request.path,
      method: request.method,
      routePath: context.routePath,
    });
  }
};
