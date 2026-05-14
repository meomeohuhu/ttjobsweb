CREATE TABLE IF NOT EXISTS forum_posts (
    id BIGSERIAL PRIMARY KEY,
    author_id BIGINT REFERENCES users(id) ON DELETE SET NULL,
    title VARCHAR(255) NOT NULL,
    body TEXT NOT NULL,
    tag VARCHAR(100) NOT NULL,
    hashtags TEXT,
    like_count INTEGER NOT NULL DEFAULT 0,
    comment_count INTEGER NOT NULL DEFAULT 0,
    hidden BOOLEAN NOT NULL DEFAULT FALSE,
    deleted_at TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS forum_comments (
    id BIGSERIAL PRIMARY KEY,
    post_id BIGINT NOT NULL REFERENCES forum_posts(id) ON DELETE CASCADE,
    author_id BIGINT REFERENCES users(id) ON DELETE SET NULL,
    body TEXT NOT NULL,
    hidden BOOLEAN NOT NULL DEFAULT FALSE,
    deleted_at TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS forum_likes (
    id BIGSERIAL PRIMARY KEY,
    post_id BIGINT NOT NULL REFERENCES forum_posts(id) ON DELETE CASCADE,
    user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_forum_likes_post_user UNIQUE (post_id, user_id)
);

CREATE TABLE IF NOT EXISTS forum_reports (
    id BIGSERIAL PRIMARY KEY,
    post_id BIGINT REFERENCES forum_posts(id) ON DELETE CASCADE,
    comment_id BIGINT REFERENCES forum_comments(id) ON DELETE CASCADE,
    reporter_id BIGINT REFERENCES users(id) ON DELETE SET NULL,
    reason VARCHAR(255) NOT NULL,
    details TEXT,
    status VARCHAR(50) NOT NULL DEFAULT 'OPEN',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    resolved_at TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_forum_posts_visible_created ON forum_posts (hidden, deleted_at, created_at DESC);
CREATE INDEX IF NOT EXISTS idx_forum_posts_tag_created ON forum_posts (tag, hidden, deleted_at, created_at DESC);
CREATE INDEX IF NOT EXISTS idx_forum_comments_post_created ON forum_comments (post_id, hidden, deleted_at, created_at);
CREATE INDEX IF NOT EXISTS idx_forum_likes_post ON forum_likes (post_id);
CREATE INDEX IF NOT EXISTS idx_forum_likes_user ON forum_likes (user_id);
CREATE INDEX IF NOT EXISTS idx_forum_reports_status ON forum_reports (status, created_at DESC);
