'use client';

import { useRouter } from 'next/navigation';
import { SearchHistory } from './SearchHistory';
import { RecentlyViewedCompact } from './RecentlyViewedCompact';

export function SearchEmptyState() {
  const router = useRouter();

  const handleHistorySelect = (query: string) => {
    router.push(`/search?q=${encodeURIComponent(query)}`);
  };

  return (
    <div>
      {/* Search History */}
      <SearchHistory onSelect={handleHistorySelect} />

      {/* Recently Viewed */}
      <RecentlyViewedCompact />

      {/* Default prompt when no history/recently viewed */}
      <div className="text-center py-12">
        <div className="text-6xl mb-4">
          <svg
            className="w-24 h-24 mx-auto text-[var(--text-secondary)] opacity-50"
            fill="none"
            stroke="currentColor"
            viewBox="0 0 24 24"
          >
            <path
              strokeLinecap="round"
              strokeLinejoin="round"
              strokeWidth={1.5}
              d="M21 21l-6-6m2-5a7 7 0 11-14 0 7 7 0 0114 0z"
            />
          </svg>
        </div>
        <h2 className="text-xl font-semibold text-[var(--text-primary)] mb-2">
          Start searching
        </h2>
        <p className="text-[var(--text-secondary)] max-w-md mx-auto">
          Enter a keyword to find recipes, cooking logs, and hashtags. Try searching for ingredients, dish names, or cooking styles.
        </p>
      </div>
    </div>
  );
}
