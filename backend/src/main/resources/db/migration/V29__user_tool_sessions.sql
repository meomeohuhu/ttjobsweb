CREATE TABLE IF NOT EXISTS user_tool_sessions (
    id          BIGSERIAL PRIMARY KEY,
    user_id     BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    tool_slug   VARCHAR(60) NOT NULL,
    input_json  TEXT NOT NULL,
    result_json TEXT NOT NULL,
    created_at  TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_tool_sessions_user
    ON user_tool_sessions(user_id, created_at DESC);

CREATE INDEX IF NOT EXISTS idx_tool_sessions_user_tool
    ON user_tool_sessions(user_id, tool_slug, created_at DESC);
