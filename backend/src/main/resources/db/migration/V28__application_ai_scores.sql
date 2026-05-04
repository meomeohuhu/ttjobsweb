CREATE TABLE IF NOT EXISTS application_ai_scores (
    id BIGSERIAL PRIMARY KEY,
    application_id BIGINT NOT NULL UNIQUE,
    score INTEGER NOT NULL,
    level VARCHAR(50) NOT NULL,
    raw_score DOUBLE PRECISION,
    signals TEXT,
    cv_hash VARCHAR(64) NOT NULL,
    job_hash VARCHAR(64) NOT NULL,
    scored_at TIMESTAMP NOT NULL,
    CONSTRAINT fk_application_ai_scores_application
        FOREIGN KEY (application_id) REFERENCES job_applications(id) ON DELETE CASCADE
);

CREATE INDEX IF NOT EXISTS idx_application_ai_scores_score
    ON application_ai_scores(score DESC, scored_at DESC);
