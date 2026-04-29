-- Recruiter activity history for dashboard feed.
CREATE TABLE IF NOT EXISTS recruiter_activity_logs (
    id BIGSERIAL PRIMARY KEY,
    actor_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    company_id BIGINT REFERENCES companies(id) ON DELETE SET NULL,
    job_id BIGINT REFERENCES jobs(id) ON DELETE SET NULL,
    application_id BIGINT REFERENCES job_applications(id) ON DELETE SET NULL,
    action_type VARCHAR(60) NOT NULL,
    title VARCHAR(255) NOT NULL,
    details VARCHAR(1000),
    created_at TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_recruiter_activity_logs_actor_created_at
    ON recruiter_activity_logs (actor_id, created_at DESC);

CREATE INDEX IF NOT EXISTS idx_recruiter_activity_logs_company
    ON recruiter_activity_logs (company_id);

CREATE INDEX IF NOT EXISTS idx_recruiter_activity_logs_job
    ON recruiter_activity_logs (job_id);
