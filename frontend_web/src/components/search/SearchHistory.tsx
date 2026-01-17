'use client';

import { useState, useEffect, useCallback } from 'react';
import { recordSearchHistory as recordSearchHistoryToBackend } from '@/lib/api/history';

const STORAGE_KEY = 'searchHistory';
const MAX_HISTORY_ITEMS = 10;

interface SearchHistoryProps {
  onSelect: (query: string) => void;
}

export function SearchHistory({ onSelect }: SearchHistoryProps) {
  // Initialize with null to detect if we've loaded from localStorage yet
  const [history, setHistory] = useState<string[] | null>(null);

  const loadHistory = useCallback(() => {
    const stored = localStorage.getItem(STORAGE_KEY);
    if (stored) {
      try {
        const parsed = JSON.parse(stored);
        if (Array.isArray(parsed)) {
          setHistory(parsed);
          return;
        }
      } catch {
        // Invalid data, reset
        localStorage.removeItem(STORAGE_KEY);
      }
    }
    setHistory([]);
  }, []);

  useEffect(() => {
    loadHistory();
  }, [loadHistory]);

  const removeItem = (query: string) => {
    if (!history) return;
    const newHistory = history.filter((item) => item !== query);
    setHistory(newHistory);
    localStorage.setItem(STORAGE_KEY, JSON.stringify(newHistory));
  };

  const clearAll = () => {
    setHistory([]);
    localStorage.removeItem(STORAGE_KEY);
  };

  // Don't render on server or if no history
  if (history === null || history.length === 0) {
    return null;
  }

  return (
    <div className="mb-8">
      <div className="flex items-center justify-between mb-3">
        <h2 className="text-lg font-semibold text-[var(--text-primary)]">
          Recent Searches
        </h2>
        <button
          onClick={clearAll}
          className="text-sm text-[var(--text-secondary)] hover:text-[var(--text-primary)] transition-colors"
        >
          Clear all
        </button>
      </div>
      <div className="flex flex-wrap gap-2">
        {history.map((query) => (
          <div
            key={query}
            className="flex items-center gap-1 px-3 py-1.5 bg-[var(--surface)] border border-[var(--border)] rounded-full text-sm text-[var(--text-primary)] hover:bg-[var(--highlight-bg)] transition-colors group"
          >
            <button
              onClick={() => onSelect(query)}
              className="hover:underline"
            >
              {query}
            </button>
            <button
              onClick={(e) => {
                e.stopPropagation();
                removeItem(query);
              }}
              className="p-0.5 rounded-full hover:bg-[var(--border)] transition-colors opacity-50 group-hover:opacity-100"
              aria-label={`Remove "${query}" from history`}
            >
              <svg
                className="w-3.5 h-3.5"
                fill="none"
                stroke="currentColor"
                viewBox="0 0 24 24"
              >
                <path
                  strokeLinecap="round"
                  strokeLinejoin="round"
                  strokeWidth={2}
                  d="M6 18L18 6M6 6l12 12"
                />
              </svg>
            </button>
          </div>
        ))}
      </div>
    </div>
  );
}

/**
 * Add a search query to the history.
 * Saves to localStorage for display and to backend for permanent storage.
 * Deduplicates and limits to MAX_HISTORY_ITEMS in localStorage.
 */
export function addToSearchHistory(query: string): void {
  if (typeof window === 'undefined') return;

  const trimmed = query.trim();
  if (!trimmed) return;

  // Save to localStorage for display
  const stored = localStorage.getItem(STORAGE_KEY);
  let history: string[] = [];

  if (stored) {
    try {
      const parsed = JSON.parse(stored);
      if (Array.isArray(parsed)) {
        history = parsed;
      }
    } catch {
      // Invalid data, reset
    }
  }

  // Remove if already exists (to move to front)
  history = history.filter((item) => item.toLowerCase() !== trimmed.toLowerCase());

  // Add to front
  history.unshift(trimmed);

  // Limit to max items
  history = history.slice(0, MAX_HISTORY_ITEMS);

  localStorage.setItem(STORAGE_KEY, JSON.stringify(history));

  // Also save to backend for permanent storage (fire and forget)
  recordSearchHistoryToBackend(trimmed);
}
