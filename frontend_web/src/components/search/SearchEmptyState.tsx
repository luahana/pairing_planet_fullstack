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
    </div>
  );
}
