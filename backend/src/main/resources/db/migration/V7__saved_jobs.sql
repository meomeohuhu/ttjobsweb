CREATE TABLE IF NOT EXISTS saved_jobs (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    job_id BIGINT NOT NULL REFERENCES jobs(id) ON DELETE CASCADE,
    saved_at TIMESTAMP NOT NULL DEFAULT NOW(),
    CONSTRAINT uq_saved_jobs_user_job UNIQUE (user_id, job_id)
);

CREATE INDEX IF NOT EXISTS idx_saved_jobs_user_saved_at ON saved_jobs(user_id, saved_at DESC);
CREATE INDEX IF NOT EXISTS idx_saved_jobs_job_id ON saved_jobs(job_id);
