CREATE TABLE IF NOT EXISTS job_alert_history (
    id      BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    job_id  BIGINT NOT NULL REFERENCES jobs(id) ON DELETE CASCADE,
    sent_at TIMESTAMP NOT NULL DEFAULT NOW(),
    CONSTRAINT uq_job_alert_history_user_job UNIQUE (user_id, job_id)
);

CREATE INDEX IF NOT EXISTS idx_job_alert_history_user_sent
    ON job_alert_history(user_id, sent_at DESC);
