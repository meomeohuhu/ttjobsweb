-- Job alerts, notification preferences, and saved job notes/tags.

CREATE TABLE IF NOT EXISTS job_alerts (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    keyword VARCHAR(200),
    location VARCHAR(200),
    job_type VARCHAR(100),
    experience_level VARCHAR(100),
    frequency VARCHAR(20) NOT NULL DEFAULT 'DAILY',
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    last_run_at TIMESTAMP
);

CREATE TABLE IF NOT EXISTS job_alert_skills (
    alert_id BIGINT NOT NULL REFERENCES job_alerts(id) ON DELETE CASCADE,
    skill VARCHAR(100) NOT NULL
);

CREATE INDEX IF NOT EXISTS idx_job_alerts_user ON job_alerts(user_id);
CREATE INDEX IF NOT EXISTS idx_job_alerts_active ON job_alerts(is_active);
CREATE INDEX IF NOT EXISTS idx_job_alert_skills_alert ON job_alert_skills(alert_id);

CREATE TABLE IF NOT EXISTS notification_preferences (
    user_id BIGINT PRIMARY KEY REFERENCES users(id) ON DELETE CASCADE,
    in_app_enabled BOOLEAN NOT NULL DEFAULT TRUE,
    email_enabled BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP NOT NULL DEFAULT NOW()
);

ALTER TABLE saved_jobs
    ADD COLUMN IF NOT EXISTS note TEXT,
    ADD COLUMN IF NOT EXISTS tag VARCHAR(100);
