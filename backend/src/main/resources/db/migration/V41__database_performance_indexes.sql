-- Performance indexes for production-like read paths.
-- These indexes are intentionally conservative and use IF NOT EXISTS so the
-- migration is safe across local databases that may already have partial work.

-- Auth/profile lookup.
CREATE INDEX IF NOT EXISTS idx_users_email ON users(email);

-- Job search and public feed filters.
CREATE INDEX IF NOT EXISTS idx_jobs_public_feed
    ON jobs(status, deleted_at, application_deadline, posted_date DESC);

CREATE INDEX IF NOT EXISTS idx_jobs_category_status_posted
    ON jobs(category, status, deleted_at, posted_date DESC);

CREATE INDEX IF NOT EXISTS idx_jobs_type_status
    ON jobs(job_type, status, deleted_at);

CREATE INDEX IF NOT EXISTS idx_jobs_experience_status
    ON jobs(experience_level, status, deleted_at);

CREATE INDEX IF NOT EXISTS idx_companies_verification_deleted
    ON companies(verification_status, deleted_at);

CREATE INDEX IF NOT EXISTS idx_companies_created_by_deleted
    ON companies(created_by, deleted_at);

-- Skill join tables used by profile/job matching and filters.
CREATE INDEX IF NOT EXISTS idx_job_skills_job ON job_skills(job_id);
CREATE INDEX IF NOT EXISTS idx_job_skills_skill ON job_skills(skill_id);
CREATE INDEX IF NOT EXISTS idx_user_skills_user ON user_skills(user_id);
CREATE INDEX IF NOT EXISTS idx_user_skills_skill ON user_skills(skill_id);

-- Applications and recruiter/candidate dashboards.
CREATE INDEX IF NOT EXISTS idx_job_applications_user_job
    ON job_applications(user_id, job_id);

CREATE INDEX IF NOT EXISTS idx_job_applications_job_status
    ON job_applications(job_id, status);

CREATE INDEX IF NOT EXISTS idx_job_applications_user_status
    ON job_applications(user_id, status);

-- Messaging realtime: latest messages, unread counts, attachments.
CREATE INDEX IF NOT EXISTS idx_messages_conversation_created
    ON messages(conversation_id, created_at DESC);

CREATE INDEX IF NOT EXISTS idx_messages_conversation_sender_created
    ON messages(conversation_id, sender_id, created_at DESC);

CREATE INDEX IF NOT EXISTS idx_message_attachments_message
    ON message_attachments(message_id);

CREATE INDEX IF NOT EXISTS idx_conversation_members_user_conversation
    ON conversation_members(user_id, conversation_id);

-- Saved searches and job alerts.
CREATE INDEX IF NOT EXISTS idx_saved_searches_user_updated
    ON saved_searches(user_id, updated_at DESC);

CREATE INDEX IF NOT EXISTS idx_saved_searches_active_updated
    ON saved_searches(active, updated_at DESC);

CREATE INDEX IF NOT EXISTS idx_job_alert_history_saved_search
    ON job_alert_history(saved_search_id, sent_at DESC);

-- Interview and recruitment analytics.
CREATE INDEX IF NOT EXISTS idx_interview_schedules_candidate_scheduled
    ON interview_schedules(candidate_id, scheduled_at ASC);

CREATE INDEX IF NOT EXISTS idx_interview_schedules_recruiter_scheduled
    ON interview_schedules(recruiter_id, scheduled_at ASC);

CREATE INDEX IF NOT EXISTS idx_interview_schedules_application
    ON interview_schedules(application_id);

CREATE INDEX IF NOT EXISTS idx_interview_schedules_scheduled
    ON interview_schedules(scheduled_at);

CREATE INDEX IF NOT EXISTS idx_recruitment_campaigns_company_created
    ON recruitment_campaigns(company_id, created_at DESC);

CREATE INDEX IF NOT EXISTS idx_recruitment_events_job_type_created
    ON recruitment_events(job_id, event_type, created_at);

CREATE INDEX IF NOT EXISTS idx_recruitment_events_type_created
    ON recruitment_events(event_type, created_at);

-- AI monitoring and export filters.
CREATE INDEX IF NOT EXISTS idx_ai_match_events_label_created
    ON ai_match_events(predicted_label, created_at DESC);

CREATE INDEX IF NOT EXISTS idx_ai_match_events_type_label_created
    ON ai_match_events(event_type, predicted_label, created_at DESC);

CREATE INDEX IF NOT EXISTS idx_ai_match_events_score_created
    ON ai_match_events(predicted_score, created_at DESC);

CREATE INDEX IF NOT EXISTS idx_ai_service_call_logs_status_created
    ON ai_service_call_logs(status, created_at DESC);

CREATE INDEX IF NOT EXISTS idx_ai_service_call_logs_fallback_created
    ON ai_service_call_logs(fallback_used, created_at DESC);

CREATE INDEX IF NOT EXISTS idx_ai_service_call_logs_label
    ON ai_service_call_logs(predicted_label);

-- CV/profile and email verification flows.
CREATE INDEX IF NOT EXISTS idx_user_cvs_user_uploaded
    ON user_cvs(user_id, uploaded_at DESC);

CREATE INDEX IF NOT EXISTS idx_email_change_user_created
    ON email_change_verifications(user_id, created_at DESC);

CREATE INDEX IF NOT EXISTS idx_email_change_user_email_unused
    ON email_change_verifications(user_id, new_email, created_at DESC)
    WHERE used_at IS NULL;
