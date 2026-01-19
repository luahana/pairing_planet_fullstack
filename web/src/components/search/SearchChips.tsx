'use client';

import { useRouter, useSearchParams } from 'next/navigation';
import type { SearchCounts, SearchTypeFilter } from '@/lib/types';

interface SearchChipsProps {
  counts: SearchCounts;
  selected: SearchTypeFilter;
  query: string;
}

const CHIP_CONFIG: Array<{ key: SearchTypeFilter; label: string; countKey: keyof SearchCounts }> = [
  { key: 'all', label: 'All', countKey: 'total' },
  { key: 'recipes', label: 'Recipes', countKey: 'recipes' },
  { key: 'logs', label: 'Logs', countKey: 'logs' },
  { key: 'hashtags', label: 'Hashtags', countKey: 'hashtags' },
];

export function SearchChips({ counts, selected, query }: SearchChipsProps) {
  const router = useRouter();
  const searchParams = useSearchParams();

  const handleSelect = (type: SearchTypeFilter) => {
    const params = new URLSearchParams(searchParams.toString());
    params.set('q', query);
    if (type === 'all') {
      params.delete('type');
    } else {
      params.set('type', type);
    }
    params.delete('page'); // Reset to first page on filter change
    router.push(`/search?${params.toString()}`);
  };

  return (
    <div className="flex flex-wrap gap-2 mb-6">
      {CHIP_CONFIG.map(({ key, label, countKey }) => {
        const count = counts[countKey];
        const isSelected = selected === key;

        return (
          <button
            key={key}
            onClick={() => handleSelect(key)}
            className={`
              inline-flex items-center px-4 py-2 rounded-full text-sm font-medium
              transition-all duration-200 border
              ${
                isSelected
                  ? 'bg-[var(--primary)] text-white border-[var(--primary)]'
                  : 'bg-[var(--surface)] text-[var(--text-secondary)] border-[var(--border)] hover:border-[var(--primary)] hover:text-[var(--text-primary)]'
              }
            `}
          >
            <span>{label}</span>
            <span
              className={`
                ml-2 px-2 py-0.5 rounded-full text-xs font-semibold
                ${
                  isSelected
                    ? 'bg-white/20 text-white'
                    : 'bg-[var(--hover-bg)] text-[var(--text-secondary)]'
                }
              `}
            >
              {count.toLocaleString()}
            </span>
          </button>
        );
      })}
    </div>
  );
}
