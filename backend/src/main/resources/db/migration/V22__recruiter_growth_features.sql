CREATE TABLE interview_schedules (
    id BIGSERIAL PRIMARY KEY,
    application_id BIGINT NOT NULL,
    recruiter_id BIGINT NOT NULL,
    candidate_id BIGINT NOT NULL,
    scheduled_at TIMESTAMP NOT NULL,
    duration_minutes INTEGER NOT NULL DEFAULT 30,
    location VARCHAR(255),
    meeting_link VARCHAR(500),
    note TEXT,
    status VARCHAR(50) NOT NULL DEFAULT 'pending',
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    CONSTRAINT fk_interview_schedules_application
        FOREIGN KEY (application_id) REFERENCES job_applications(id)
        ON DELETE CASCADE,
    CONSTRAINT fk_interview_schedules_recruiter
        FOREIGN KEY (recruiter_id) REFERENCES users(id),
    CONSTRAINT fk_interview_schedules_candidate
        FOREIGN KEY (candidate_id) REFERENCES users(id)
);

CREATE TABLE recruitment_campaigns (
    id BIGSERIAL PRIMARY KEY,
    company_id BIGINT NOT NULL,
    created_by_id BIGINT NOT NULL,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    status VARCHAR(50) NOT NULL DEFAULT 'active',
    target_hires INTEGER,
    starts_at TIMESTAMP,
    ends_at TIMESTAMP,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    CONSTRAINT fk_recruitment_campaigns_company
        FOREIGN KEY (company_id) REFERENCES companies(id)
        ON DELETE CASCADE,
    CONSTRAINT fk_recruitment_campaigns_created_by
        FOREIGN KEY (created_by_id) REFERENCES users(id)
);

CREATE TABLE recruitment_campaign_jobs (
    campaign_id BIGINT NOT NULL,
    job_id BIGINT NOT NULL,
    PRIMARY KEY (campaign_id, job_id),
    CONSTRAINT fk_campaign_jobs_campaign
        FOREIGN KEY (campaign_id) REFERENCES recruitment_campaigns(id)
        ON DELETE CASCADE,
    CONSTRAINT fk_campaign_jobs_job
        FOREIGN KEY (job_id) REFERENCES jobs(id)
        ON DELETE CASCADE
);

CREATE TABLE recruitment_campaign_applications (
    campaign_id BIGINT NOT NULL,
    application_id BIGINT NOT NULL,
    PRIMARY KEY (campaign_id, application_id),
    CONSTRAINT fk_campaign_applications_campaign
        FOREIGN KEY (campaign_id) REFERENCES recruitment_campaigns(id)
        ON DELETE CASCADE,
    CONSTRAINT fk_campaign_applications_application
        FOREIGN KEY (application_id) REFERENCES job_applications(id)
        ON DELETE CASCADE
);

CREATE TABLE recruitment_events (
    id BIGSERIAL PRIMARY KEY,
    company_id BIGINT,
    job_id BIGINT,
    application_id BIGINT,
    actor_id BIGINT,
    event_type VARCHAR(100) NOT NULL,
    created_at TIMESTAMP NOT NULL,
    metadata TEXT,
    CONSTRAINT fk_recruitment_events_company
        FOREIGN KEY (company_id) REFERENCES companies(id)
        ON DELETE SET NULL,
    CONSTRAINT fk_recruitment_events_job
        FOREIGN KEY (job_id) REFERENCES jobs(id)
        ON DELETE SET NULL,
    CONSTRAINT fk_recruitment_events_application
        FOREIGN KEY (application_id) REFERENCES job_applications(id)
        ON DELETE SET NULL,
    CONSTRAINT fk_recruitment_events_actor
        FOREIGN KEY (actor_id) REFERENCES users(id)
        ON DELETE SET NULL
);
