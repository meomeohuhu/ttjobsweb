-- Track status transitions for job applications.
CREATE TABLE IF NOT EXISTS job_application_status_audits (
    id BIGSERIAL PRIMARY KEY,
    application_id BIGINT NOT NULL REFERENCES job_applications(id) ON DELETE CASCADE,
    changed_by_id BIGINT NOT NULL REFERENCES users(id),
    from_status VARCHAR(50),
    to_status VARCHAR(50) NOT NULL,
    changed_at TIMESTAMP NOT NULL DEFAULT now()
);

CREATE INDEX IF NOT EXISTS idx_job_app_status_audit_application
    ON job_application_status_audits (application_id);
