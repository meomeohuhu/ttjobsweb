CREATE TABLE IF NOT EXISTS job_need_preferences (
    user_id BIGINT PRIMARY KEY,
    desired_title VARCHAR(255),
    desired_location VARCHAR(255),
    desired_category VARCHAR(255),
    desired_job_type VARCHAR(120),
    desired_experience_level VARCHAR(120),
    min_salary NUMERIC(19, 2),
    max_salary NUMERIC(19, 2),
    remote_only BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);
