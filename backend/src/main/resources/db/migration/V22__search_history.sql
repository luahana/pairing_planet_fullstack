-- Search History table for tracking user search queries
CREATE TABLE search_history (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    query VARCHAR(500) NOT NULL,
    searched_at TIMESTAMP WITH TIME ZONE DEFAULT NOW() NOT NULL
);

-- Index for efficient lookups by user (most recent first)
CREATE INDEX idx_search_history_user_searched ON search_history (user_id, searched_at DESC);
