# TTJobs Database ERD

Generated from the local PostgreSQL schema after Flyway migration `V42`.

```mermaid
erDiagram
    USERS {
        bigint id PK
        varchar email
        varchar name
        varchar role
        text cv_text
        varchar cv_role
        int experience_years
        timestamp created_at
    }

    COMPANIES {
        bigint id PK
        bigint created_by FK
        varchar name
        varchar industry
        varchar location
        varchar website
        varchar verification_status
        timestamp deleted_at
    }

    COMPANY_MEMBERS {
        bigint id PK
        bigint company_id FK
        bigint user_id FK
        varchar member_role
        timestamp created_at
    }

    COMPANY_VERIFICATIONS {
        bigint id PK
        bigint company_id FK
        bigint reviewed_by FK
        varchar tax_code
        varchar business_license_url
        varchar status
        text review_reason
        timestamp reviewed_at
    }

    COMPANY_FOLLOWS {
        bigint id PK
        bigint user_id FK
        bigint company_id FK
        timestamp followed_at
    }

    COMPANY_REVIEWS {
        bigint id PK
        bigint company_id FK
        bigint user_id FK
        int rating
        text pros
        text cons
        boolean anonymous
    }

    JOBS {
        bigint id PK
        bigint company_id FK
        varchar title
        varchar category
        varchar status
        varchar job_type
        varchar experience_level
        numeric salary_min
        numeric salary_max
        timestamp posted_date
        timestamp deleted_at
    }

    JOB_APPLICATIONS {
        bigint id PK
        bigint job_id FK
        bigint user_id FK
        bigint cv_id FK
        varchar status
        text cover_letter
        text cv_text_snapshot
        timestamp application_date
    }

    JOB_APPLICATION_STATUS_AUDITS {
        bigint id PK
        bigint application_id FK
        bigint changed_by_id FK
        varchar from_status
        varchar to_status
        timestamp changed_at
    }

    SKILLS {
        bigint id PK
        varchar name
    }

    JOB_SKILLS {
        bigint job_id FK
        bigint skill_id FK
    }

    USER_SKILLS {
        bigint user_id FK
        bigint skill_id FK
    }

    USER_CVS {
        bigint id PK
        bigint user_id FK
        varchar cv_url
        varchar file_name
        timestamp uploaded_at
    }

    SAVED_JOBS {
        bigint id PK
        bigint user_id FK
        bigint job_id FK
        varchar tag
        text note
        timestamp saved_at
    }

    SAVED_SEARCHES {
        bigint id PK
        bigint user_id FK
        varchar name
        varchar keyword
        varchar location
        varchar category
        varchar job_type
        numeric salary_min
        numeric salary_max
        boolean active
        text excluded_keywords
    }

    JOB_ALERT_HISTORY {
        bigint id PK
        bigint user_id FK
        bigint job_id FK
        bigint saved_search_id FK
        timestamp sent_at
    }

    NOTIFICATIONS {
        bigint id PK
        bigint user_id FK
        varchar title
        text content
        varchar type
        boolean is_read
        varchar target_url
    }

    NOTIFICATION_PREFERENCES {
        bigint user_id PK_FK
        boolean in_app_enabled
        boolean email_enabled
    }

    CONVERSATIONS {
        bigint id PK
        timestamp created_at
    }

    CONVERSATION_MEMBERS {
        bigint conversation_id FK
        bigint user_id FK
        timestamp last_read_at
    }

    MESSAGES {
        bigint id PK
        bigint conversation_id FK
        bigint sender_id FK
        text content
        varchar type
        timestamp created_at
    }

    MESSAGE_ATTACHMENTS {
        bigint id PK
        bigint message_id FK
        varchar file_name
        text file_url
        varchar mime_type
        bigint file_size
    }

    FORUM_POSTS {
        bigint id PK
        bigint author_id FK
        varchar title
        text body
        varchar tag
        text hashtags
        int like_count
        int comment_count
        boolean hidden
        timestamp deleted_at
    }

    FORUM_COMMENTS {
        bigint id PK
        bigint post_id FK
        bigint author_id FK
        text body
        boolean hidden
        timestamp deleted_at
    }

    FORUM_LIKES {
        bigint id PK
        bigint post_id FK
        bigint user_id FK
        timestamp created_at
    }

    FORUM_REPORTS {
        bigint id PK
        bigint post_id FK
        bigint comment_id FK
        bigint reporter_id FK
        bigint resolved_by FK
        varchar reason
        varchar status
        varchar moderation_action
    }

    FORUM_USER_VIOLATIONS {
        bigint id PK
        bigint user_id FK
        int warning_count
        timestamp muted_until
        text last_reason
    }

    INTERVIEW_SCHEDULES {
        bigint id PK
        bigint application_id FK
        bigint recruiter_id FK
        bigint candidate_id FK
        timestamp scheduled_at
        varchar status
    }

    INTERVIEW_ROOMS {
        bigint id PK
        bigint interview_id FK
        varchar room_id
        varchar status
        timestamp started_at
        timestamp ended_at
    }

    RECRUITMENT_CAMPAIGNS {
        bigint id PK
        bigint company_id FK
        bigint created_by_id FK
        varchar name
        varchar status
        int target_hires
    }

    RECRUITMENT_CAMPAIGN_JOBS {
        bigint campaign_id FK
        bigint job_id FK
    }

    RECRUITMENT_CAMPAIGN_APPLICATIONS {
        bigint campaign_id FK
        bigint application_id FK
    }

    RECRUITMENT_EVENTS {
        bigint id PK
        bigint company_id FK
        bigint job_id FK
        bigint application_id FK
        bigint actor_id FK
        varchar event_type
        text metadata
    }

    RECRUITER_ACTIVITY_LOGS {
        bigint id PK
        bigint actor_id FK
        bigint company_id FK
        bigint job_id FK
        bigint application_id FK
        varchar action_type
        varchar title
    }

    ADMIN_AUDIT_LOGS {
        bigint id PK
        bigint actor_id FK
        varchar action
        varchar target_type
        bigint target_id
        text reason
        text metadata
    }

    AI_MATCH_EVENTS {
        bigint id PK
        bigint user_id FK
        bigint job_id FK
        varchar event_type
        varchar predicted_label
        int predicted_score
        varchar source
    }

    AI_SERVICE_CALL_LOGS {
        bigint id PK
        varchar endpoint
        varchar status
        int http_status
        bigint latency_ms
        boolean fallback_used
        varchar predicted_label
    }

    APPLICATION_AI_SCORES {
        bigint id PK
        bigint application_id FK
        int score
        varchar level
        float raw_score
        text signals
    }

    CANDIDATE_JOB_MATCHES {
        bigint id PK
        bigint user_id FK
        bigint job_id FK
        int score
        text reasons
    }

    CANDIDATE_PROFILE_SIGNALS {
        bigint id PK
        bigint user_id FK
        varchar normalized_title
        varchar seniority
        text skills
        text industries
        text evidence
    }

    JOB_REQUIREMENT_SIGNALS {
        bigint id PK
        bigint job_id FK
        varchar normalized_title
        varchar seniority
        text skills
        text industries
        text evidence
    }

    CAREER_GUIDE_ARTICLES {
        bigint id PK
        varchar slug
        varchar title
        varchar category
        boolean featured
        timestamp published_at
    }

    USER_TOOL_SESSIONS {
        bigint id PK
        bigint user_id FK
        varchar tool_slug
        text input_json
        text result_json
    }

    EMAIL_CHANGE_VERIFICATIONS {
        bigint id PK
        bigint user_id FK
        varchar new_email
        varchar code
        timestamp expires_at
        timestamp used_at
    }

    USERS ||--o{ COMPANIES : creates
    USERS ||--o{ COMPANY_MEMBERS : belongs_to
    COMPANIES ||--o{ COMPANY_MEMBERS : has_members
    COMPANIES ||--o{ COMPANY_VERIFICATIONS : verifies
    USERS ||--o{ COMPANY_VERIFICATIONS : reviews
    USERS ||--o{ COMPANY_FOLLOWS : follows
    COMPANIES ||--o{ COMPANY_FOLLOWS : followed_by
    USERS ||--o{ COMPANY_REVIEWS : writes
    COMPANIES ||--o{ COMPANY_REVIEWS : receives

    COMPANIES ||--o{ JOBS : posts
    JOBS ||--o{ JOB_APPLICATIONS : receives
    USERS ||--o{ JOB_APPLICATIONS : applies
    USER_CVS ||--o{ JOB_APPLICATIONS : selected_cv
    JOB_APPLICATIONS ||--o{ JOB_APPLICATION_STATUS_AUDITS : status_history
    USERS ||--o{ JOB_APPLICATION_STATUS_AUDITS : changes
    JOBS ||--o{ JOB_SKILLS : requires
    SKILLS ||--o{ JOB_SKILLS : mapped_to_jobs
    USERS ||--o{ USER_SKILLS : owns
    SKILLS ||--o{ USER_SKILLS : mapped_to_users
    USERS ||--o{ USER_CVS : uploads
    USERS ||--o{ SAVED_JOBS : saves
    JOBS ||--o{ SAVED_JOBS : saved_by
    USERS ||--o{ SAVED_SEARCHES : owns
    USERS ||--o{ JOB_ALERT_HISTORY : receives
    JOBS ||--o{ JOB_ALERT_HISTORY : alerted
    SAVED_SEARCHES ||--o{ JOB_ALERT_HISTORY : creates

    USERS ||--o{ NOTIFICATIONS : receives
    USERS ||--|| NOTIFICATION_PREFERENCES : configures

    CONVERSATIONS ||--o{ CONVERSATION_MEMBERS : has_members
    USERS ||--o{ CONVERSATION_MEMBERS : joins
    CONVERSATIONS ||--o{ MESSAGES : contains
    USERS ||--o{ MESSAGES : sends
    MESSAGES ||--o{ MESSAGE_ATTACHMENTS : has_files

    USERS ||--o{ FORUM_POSTS : authors
    FORUM_POSTS ||--o{ FORUM_COMMENTS : has_comments
    USERS ||--o{ FORUM_COMMENTS : writes
    FORUM_POSTS ||--o{ FORUM_LIKES : receives
    USERS ||--o{ FORUM_LIKES : likes
    FORUM_POSTS ||--o{ FORUM_REPORTS : reported_post
    FORUM_COMMENTS ||--o{ FORUM_REPORTS : reported_comment
    USERS ||--o{ FORUM_REPORTS : reports
    USERS ||--o{ FORUM_REPORTS : resolves
    USERS ||--o{ FORUM_USER_VIOLATIONS : moderation_state

    JOB_APPLICATIONS ||--o{ INTERVIEW_SCHEDULES : schedules
    USERS ||--o{ INTERVIEW_SCHEDULES : recruiter
    USERS ||--o{ INTERVIEW_SCHEDULES : candidate
    INTERVIEW_SCHEDULES ||--o{ INTERVIEW_ROOMS : opens_room

    COMPANIES ||--o{ RECRUITMENT_CAMPAIGNS : owns
    USERS ||--o{ RECRUITMENT_CAMPAIGNS : creates
    RECRUITMENT_CAMPAIGNS ||--o{ RECRUITMENT_CAMPAIGN_JOBS : includes_jobs
    JOBS ||--o{ RECRUITMENT_CAMPAIGN_JOBS : in_campaigns
    RECRUITMENT_CAMPAIGNS ||--o{ RECRUITMENT_CAMPAIGN_APPLICATIONS : tracks_applications
    JOB_APPLICATIONS ||--o{ RECRUITMENT_CAMPAIGN_APPLICATIONS : in_campaigns
    COMPANIES ||--o{ RECRUITMENT_EVENTS : event_company
    JOBS ||--o{ RECRUITMENT_EVENTS : event_job
    JOB_APPLICATIONS ||--o{ RECRUITMENT_EVENTS : event_application
    USERS ||--o{ RECRUITMENT_EVENTS : actor
    USERS ||--o{ RECRUITER_ACTIVITY_LOGS : actor
    COMPANIES ||--o{ RECRUITER_ACTIVITY_LOGS : company
    JOBS ||--o{ RECRUITER_ACTIVITY_LOGS : job
    JOB_APPLICATIONS ||--o{ RECRUITER_ACTIVITY_LOGS : application

    USERS ||--o{ ADMIN_AUDIT_LOGS : admin_actor
    USERS ||--o{ AI_MATCH_EVENTS : candidate
    JOBS ||--o{ AI_MATCH_EVENTS : job
    JOB_APPLICATIONS ||--o{ APPLICATION_AI_SCORES : scored
    USERS ||--o{ CANDIDATE_JOB_MATCHES : candidate
    JOBS ||--o{ CANDIDATE_JOB_MATCHES : job
    USERS ||--o{ CANDIDATE_PROFILE_SIGNALS : normalized_profile
    JOBS ||--o{ JOB_REQUIREMENT_SIGNALS : normalized_requirement

    USERS ||--o{ USER_TOOL_SESSIONS : uses_tools
    USERS ||--o{ EMAIL_CHANGE_VERIFICATIONS : changes_email
```

## Module Groups

- Core identity: `users`, `user_cvs`, `user_skills`, `email_change_verifications`
- Company/recruiter: `companies`, `company_members`, `company_verifications`, `company_follows`, `company_reviews`
- Job/application: `jobs`, `job_applications`, `job_application_status_audits`, `job_skills`, `skills`, `saved_jobs`, `saved_searches`, `job_alert_history`
- Messaging: `conversations`, `conversation_members`, `messages`, `message_attachments`
- Forum: `forum_posts`, `forum_comments`, `forum_likes`, `forum_reports`, `forum_user_violations`
- Interview: `interview_schedules`, `interview_rooms`
- Recruitment analytics: `recruitment_campaigns`, `recruitment_campaign_jobs`, `recruitment_campaign_applications`, `recruitment_events`, `recruiter_activity_logs`
- AI/matching: `ai_match_events`, `ai_service_call_logs`, `application_ai_scores`, `candidate_job_matches`, `candidate_profile_signals`, `job_requirement_signals`
- Admin/content/tools: `admin_audit_logs`, `career_guide_articles`, `notifications`, `notification_preferences`, `user_tool_sessions`
