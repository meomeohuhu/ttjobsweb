CREATE TABLE IF NOT EXISTS ai_match_events (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT REFERENCES users(id) ON DELETE SET NULL,
    job_id BIGINT REFERENCES jobs(id) ON DELETE SET NULL,
    event_type VARCHAR(80) NOT NULL,
    cv_snapshot_text TEXT,
    job_snapshot_text TEXT,
    predicted_label VARCHAR(40),
    predicted_score INTEGER,
    source VARCHAR(80),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_ai_match_events_created ON ai_match_events(created_at DESC);
CREATE INDEX IF NOT EXISTS idx_ai_match_events_type ON ai_match_events(event_type, created_at DESC);
CREATE INDEX IF NOT EXISTS idx_ai_match_events_user_job ON ai_match_events(user_id, job_id, created_at DESC);

CREATE TABLE IF NOT EXISTS ai_service_call_logs (
    id BIGSERIAL PRIMARY KEY,
    endpoint VARCHAR(160) NOT NULL,
    status VARCHAR(40) NOT NULL,
    http_status INTEGER,
    latency_ms BIGINT NOT NULL DEFAULT 0,
    fallback_used BOOLEAN NOT NULL DEFAULT FALSE,
    predicted_label VARCHAR(40),
    confidence DOUBLE PRECISION,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_ai_service_call_logs_created ON ai_service_call_logs(created_at DESC);
CREATE INDEX IF NOT EXISTS idx_ai_service_call_logs_endpoint ON ai_service_call_logs(endpoint, created_at DESC);
