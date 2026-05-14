ALTER TABLE companies
    ADD COLUMN IF NOT EXISTS verification_status VARCHAR(30) NOT NULL DEFAULT 'PENDING';

CREATE TABLE IF NOT EXISTS company_verifications (
    id BIGSERIAL PRIMARY KEY,
    company_id BIGINT NOT NULL REFERENCES companies(id) ON DELETE CASCADE,
    business_license_url VARCHAR(1000),
    tax_code VARCHAR(100),
    website VARCHAR(500),
    note TEXT,
    status VARCHAR(30) NOT NULL DEFAULT 'PENDING',
    review_reason TEXT,
    reviewed_by BIGINT REFERENCES users(id) ON DELETE SET NULL,
    reviewed_at TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uq_company_verifications_company UNIQUE (company_id)
);

CREATE INDEX IF NOT EXISTS idx_company_verifications_status ON company_verifications(status, created_at DESC);

CREATE TABLE IF NOT EXISTS admin_audit_logs (
    id BIGSERIAL PRIMARY KEY,
    actor_id BIGINT REFERENCES users(id) ON DELETE SET NULL,
    action VARCHAR(120) NOT NULL,
    target_type VARCHAR(80),
    target_id BIGINT,
    reason TEXT,
    metadata TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_admin_audit_logs_created ON admin_audit_logs(created_at DESC);
CREATE INDEX IF NOT EXISTS idx_admin_audit_logs_target ON admin_audit_logs(target_type, target_id);

ALTER TABLE forum_reports
    ADD COLUMN IF NOT EXISTS moderation_action VARCHAR(80),
    ADD COLUMN IF NOT EXISTS moderation_reason TEXT,
    ADD COLUMN IF NOT EXISTS resolved_by BIGINT REFERENCES users(id) ON DELETE SET NULL;

UPDATE forum_reports SET status = 'PENDING' WHERE status = 'OPEN';

CREATE TABLE IF NOT EXISTS forum_user_violations (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    warning_count INTEGER NOT NULL DEFAULT 0,
    muted_until TIMESTAMP,
    last_reason TEXT,
    last_action_at TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uq_forum_user_violations_user UNIQUE (user_id)
);

CREATE INDEX IF NOT EXISTS idx_forum_user_violations_muted ON forum_user_violations(muted_until);

CREATE TABLE IF NOT EXISTS saved_searches (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    name VARCHAR(160) NOT NULL,
    keyword VARCHAR(255),
    location VARCHAR(255),
    category VARCHAR(100),
    job_type VARCHAR(100),
    experience_level VARCHAR(100),
    salary_min NUMERIC(14,2),
    salary_max NUMERIC(14,2),
    remote_only BOOLEAN,
    skills TEXT,
    alert_frequency VARCHAR(30) NOT NULL DEFAULT 'DAILY',
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_saved_searches_user ON saved_searches(user_id, active);

ALTER TABLE job_alert_history
    ADD COLUMN IF NOT EXISTS saved_search_id BIGINT REFERENCES saved_searches(id) ON DELETE SET NULL;

CREATE UNIQUE INDEX IF NOT EXISTS uq_job_alert_history_search_job
    ON job_alert_history(user_id, saved_search_id, job_id)
    WHERE saved_search_id IS NOT NULL;

CREATE TABLE IF NOT EXISTS candidate_profile_signals (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    normalized_title VARCHAR(255),
    seniority VARCHAR(50),
    skills TEXT,
    industries TEXT,
    locations TEXT,
    salary_min NUMERIC(14,2),
    salary_max NUMERIC(14,2),
    currency VARCHAR(20),
    languages TEXT,
    evidence TEXT,
    raw_text TEXT,
    source VARCHAR(50),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_candidate_profile_signals_user ON candidate_profile_signals(user_id, updated_at DESC);

CREATE TABLE IF NOT EXISTS job_requirement_signals (
    id BIGSERIAL PRIMARY KEY,
    job_id BIGINT NOT NULL REFERENCES jobs(id) ON DELETE CASCADE,
    normalized_title VARCHAR(255),
    seniority VARCHAR(50),
    skills TEXT,
    industries TEXT,
    locations TEXT,
    salary_min NUMERIC(14,2),
    salary_max NUMERIC(14,2),
    currency VARCHAR(20),
    languages TEXT,
    evidence TEXT,
    raw_text TEXT,
    source VARCHAR(50),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uq_job_requirement_signals_job UNIQUE (job_id)
);

CREATE TABLE IF NOT EXISTS interview_rooms (
    id BIGSERIAL PRIMARY KEY,
    interview_id BIGINT NOT NULL REFERENCES interview_schedules(id) ON DELETE CASCADE,
    room_id VARCHAR(120) NOT NULL UNIQUE,
    status VARCHAR(30) NOT NULL DEFAULT 'WAITING',
    started_at TIMESTAMP,
    ended_at TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uq_interview_rooms_interview UNIQUE (interview_id)
);
