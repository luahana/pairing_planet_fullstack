'use client';

import * as Sentry from '@sentry/nextjs';
import { useEffect } from 'react';

export default function GlobalError({
  error,
  reset,
}: {
  error: Error & { digest?: string };
  reset: () => void;
}) {
  useEffect(() => {
    // Capture error in Sentry (only sends if DSN is configured)
    Sentry.captureException(error);
    console.error('[Global Error]', error);
  }, [error]);

  return (
    <html>
      <body>
        <div className="min-h-screen flex items-center justify-center px-4 bg-white">
          <div className="text-center max-w-md">
            {/* Icon */}
            <div className="text-8xl mb-6">🔥</div>

            {/* Error Code */}
            <h1 className="text-6xl font-bold text-orange-500 mb-4">Oops!</h1>

            {/* Message */}
            <h2 className="text-2xl font-semibold text-gray-900 mb-2">
              Something went wrong
            </h2>
            <p className="text-gray-600 mb-8">
              We encountered an unexpected error. Our team has been notified and
              is working to fix it.
            </p>

            {/* Error digest for support */}
            {error.digest && (
              <p className="text-sm text-gray-400 mb-4 font-mono">
                Error ID: {error.digest}
              </p>
            )}

            {/* Actions */}
            <div className="flex flex-col sm:flex-row gap-4 justify-center">
              <button
                onClick={reset}
                className="px-6 py-3 bg-orange-500 text-white font-medium rounded-xl hover:bg-orange-600 transition-colors"
              >
                Try Again
              </button>
              {/* eslint-disable-next-line @next/next/no-html-link-for-pages -- global-error needs hard navigation */}
              <a
                href="/"
                className="px-6 py-3 bg-gray-100 text-gray-900 font-medium rounded-xl border border-gray-200 hover:border-orange-500 transition-colors"
              >
                Go Home
              </a>
            </div>
          </div>
        </div>
      </body>
    </html>
  );
}
