CREATE TABLE IF NOT EXISTS candidate_job_matches (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    job_id BIGINT NOT NULL REFERENCES jobs(id),
    preference_updated_at TIMESTAMP NOT NULL,
    score INTEGER NOT NULL,
    reasons TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_candidate_job_matches_user_version
    ON candidate_job_matches (user_id, preference_updated_at, score DESC);

