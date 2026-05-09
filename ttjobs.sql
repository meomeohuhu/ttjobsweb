--
-- PostgreSQL database dump
--

\restrict fI2CFUsOo3OrgLV6Kiet7K29HNDaydhcFIZfkuwgvsSO38n1Z5nBzrPNxDfk7iK

-- Dumped from database version 18.1
-- Dumped by pg_dump version 18.1

SET statement_timeout = 0;
SET lock_timeout = 0;
SET idle_in_transaction_session_timeout = 0;
SET transaction_timeout = 0;
SET client_encoding = 'UTF8';
SET standard_conforming_strings = on;
SELECT pg_catalog.set_config('search_path', '', false);
SET check_function_bodies = false;
SET xmloption = content;
SET client_min_messages = warning;
SET row_security = off;

--
-- Name: public; Type: SCHEMA; Schema: -; Owner: postgres
--

-- *not* creating schema, since initdb creates it


ALTER SCHEMA public OWNER TO postgres;

--
-- Name: SCHEMA public; Type: COMMENT; Schema: -; Owner: postgres
--

COMMENT ON SCHEMA public IS '';


--
-- Name: set_saved_jobs_saved_at(); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION public.set_saved_jobs_saved_at() RETURNS trigger
    LANGUAGE plpgsql
    AS $$
BEGIN
    IF NEW.saved_at IS NULL THEN
        NEW.saved_at := NOW();
    END IF;
    RETURN NEW;
END;
$$;


ALTER FUNCTION public.set_saved_jobs_saved_at() OWNER TO postgres;

SET default_tablespace = '';

SET default_table_access_method = heap;

--
-- Name: application_ai_scores; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.application_ai_scores (
    id bigint NOT NULL,
    application_id bigint NOT NULL,
    score integer NOT NULL,
    level character varying(50) NOT NULL,
    raw_score double precision,
    signals text,
    cv_hash character varying(64) NOT NULL,
    job_hash character varying(64) NOT NULL,
    scored_at timestamp without time zone NOT NULL,
    pros text,
    cons text
);


ALTER TABLE public.application_ai_scores OWNER TO postgres;

--
-- Name: application_ai_scores_id_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.application_ai_scores_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.application_ai_scores_id_seq OWNER TO postgres;

--
-- Name: application_ai_scores_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE public.application_ai_scores_id_seq OWNED BY public.application_ai_scores.id;


--
-- Name: candidate_job_matches; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.candidate_job_matches (
    id bigint NOT NULL,
    user_id bigint NOT NULL,
    job_id bigint NOT NULL,
    preference_updated_at timestamp without time zone NOT NULL,
    score integer NOT NULL,
    reasons text,
    created_at timestamp without time zone DEFAULT now() NOT NULL
);


ALTER TABLE public.candidate_job_matches OWNER TO postgres;

--
-- Name: candidate_job_matches_id_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.candidate_job_matches_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.candidate_job_matches_id_seq OWNER TO postgres;

--
-- Name: candidate_job_matches_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE public.candidate_job_matches_id_seq OWNED BY public.candidate_job_matches.id;


--
-- Name: career_guide_articles; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.career_guide_articles (
    id bigint NOT NULL,
    slug character varying(160) NOT NULL,
    title character varying(255) NOT NULL,
    summary character varying(500) NOT NULL,
    content text NOT NULL,
    category character varying(120) NOT NULL,
    cover_image_url character varying(500),
    reading_time_minutes integer,
    featured boolean DEFAULT false NOT NULL,
    published_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL,
    created_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL
);


ALTER TABLE public.career_guide_articles OWNER TO postgres;

--
-- Name: career_guide_articles_id_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.career_guide_articles_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.career_guide_articles_id_seq OWNER TO postgres;

--
-- Name: career_guide_articles_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE public.career_guide_articles_id_seq OWNED BY public.career_guide_articles.id;


--
-- Name: companies; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.companies (
    id bigint NOT NULL,
    description character varying(255),
    industry character varying(255),
    location character varying(255),
    name character varying(255),
    website character varying(255),
    created_at timestamp(6) without time zone,
    logo_url character varying(255),
    created_by bigint,
    updated_at timestamp without time zone,
    deleted_at timestamp without time zone
);


ALTER TABLE public.companies OWNER TO postgres;

--
-- Name: companies_id_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

ALTER TABLE public.companies ALTER COLUMN id ADD GENERATED BY DEFAULT AS IDENTITY (
    SEQUENCE NAME public.companies_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1
);


--
-- Name: company_follows; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.company_follows (
    id bigint NOT NULL,
    user_id bigint NOT NULL,
    company_id bigint NOT NULL,
    followed_at timestamp without time zone DEFAULT now() NOT NULL
);


ALTER TABLE public.company_follows OWNER TO postgres;

--
-- Name: company_follows_id_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.company_follows_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.company_follows_id_seq OWNER TO postgres;

--
-- Name: company_follows_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE public.company_follows_id_seq OWNED BY public.company_follows.id;


--
-- Name: company_members; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.company_members (
    id bigint NOT NULL,
    company_id bigint NOT NULL,
    user_id bigint NOT NULL,
    member_role character varying(20) NOT NULL,
    created_at timestamp without time zone DEFAULT now() NOT NULL,
    CONSTRAINT ck_company_members_role CHECK (((member_role)::text = ANY ((ARRAY['RECRUITER'::character varying, 'ADMIN'::character varying])::text[])))
);


ALTER TABLE public.company_members OWNER TO postgres;

--
-- Name: company_members_id_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.company_members_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.company_members_id_seq OWNER TO postgres;

--
-- Name: company_members_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE public.company_members_id_seq OWNED BY public.company_members.id;


--
-- Name: company_reviews; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.company_reviews (
    id bigint NOT NULL,
    company_id bigint NOT NULL,
    user_id bigint,
    rating integer NOT NULL,
    pros text,
    cons text,
    salary numeric(14,2),
    anonymous boolean DEFAULT true NOT NULL,
    created_at timestamp without time zone DEFAULT now() NOT NULL,
    updated_at timestamp without time zone DEFAULT now() NOT NULL
);


ALTER TABLE public.company_reviews OWNER TO postgres;

--
-- Name: company_reviews_id_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.company_reviews_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.company_reviews_id_seq OWNER TO postgres;

--
-- Name: company_reviews_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE public.company_reviews_id_seq OWNED BY public.company_reviews.id;


--
-- Name: conversation_members; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.conversation_members (
    conversation_id bigint NOT NULL,
    user_id bigint NOT NULL,
    created_at timestamp without time zone DEFAULT now() NOT NULL,
    last_read_at timestamp without time zone
);


ALTER TABLE public.conversation_members OWNER TO postgres;

--
-- Name: conversations; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.conversations (
    id bigint NOT NULL,
    created_at timestamp without time zone DEFAULT now() NOT NULL
);


ALTER TABLE public.conversations OWNER TO postgres;

--
-- Name: conversations_id_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.conversations_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.conversations_id_seq OWNER TO postgres;

--
-- Name: conversations_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE public.conversations_id_seq OWNED BY public.conversations.id;


--
-- Name: cvs; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.cvs (
    id bigint NOT NULL,
    file_url character varying(255),
    uploaded_at timestamp(6) without time zone,
    user_id bigint
);


ALTER TABLE public.cvs OWNER TO postgres;

--
-- Name: cvs_id_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

ALTER TABLE public.cvs ALTER COLUMN id ADD GENERATED BY DEFAULT AS IDENTITY (
    SEQUENCE NAME public.cvs_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1
);


--
-- Name: email_change_verifications; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.email_change_verifications (
    id bigint NOT NULL,
    user_id bigint NOT NULL,
    new_email character varying(255) NOT NULL,
    code character varying(12) NOT NULL,
    expires_at timestamp without time zone NOT NULL,
    used_at timestamp without time zone,
    created_at timestamp without time zone DEFAULT now() NOT NULL
);


ALTER TABLE public.email_change_verifications OWNER TO postgres;

--
-- Name: email_change_verifications_id_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.email_change_verifications_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.email_change_verifications_id_seq OWNER TO postgres;

--
-- Name: email_change_verifications_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE public.email_change_verifications_id_seq OWNED BY public.email_change_verifications.id;


--
-- Name: flyway_schema_history; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.flyway_schema_history (
    installed_rank integer NOT NULL,
    version character varying(50),
    description character varying(200) NOT NULL,
    type character varying(20) NOT NULL,
    script character varying(1000) NOT NULL,
    checksum integer,
    installed_by character varying(100) NOT NULL,
    installed_on timestamp without time zone DEFAULT now() NOT NULL,
    execution_time integer NOT NULL,
    success boolean NOT NULL
);


ALTER TABLE public.flyway_schema_history OWNER TO postgres;

--
-- Name: interview_schedules; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.interview_schedules (
    id bigint NOT NULL,
    application_id bigint NOT NULL,
    recruiter_id bigint NOT NULL,
    candidate_id bigint NOT NULL,
    scheduled_at timestamp without time zone NOT NULL,
    duration_minutes integer DEFAULT 30 NOT NULL,
    location character varying(255),
    meeting_link character varying(500),
    note text,
    status character varying(50) DEFAULT 'pending'::character varying NOT NULL,
    created_at timestamp without time zone NOT NULL,
    updated_at timestamp without time zone NOT NULL
);


ALTER TABLE public.interview_schedules OWNER TO postgres;

--
-- Name: interview_schedules_id_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.interview_schedules_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.interview_schedules_id_seq OWNER TO postgres;

--
-- Name: interview_schedules_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE public.interview_schedules_id_seq OWNED BY public.interview_schedules.id;


--
-- Name: job_alert_history; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.job_alert_history (
    id bigint NOT NULL,
    user_id bigint NOT NULL,
    job_id bigint NOT NULL,
    sent_at timestamp without time zone DEFAULT now() NOT NULL
);


ALTER TABLE public.job_alert_history OWNER TO postgres;

--
-- Name: job_alert_history_id_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.job_alert_history_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.job_alert_history_id_seq OWNER TO postgres;

--
-- Name: job_alert_history_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE public.job_alert_history_id_seq OWNED BY public.job_alert_history.id;


--
-- Name: job_application_status_audits; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.job_application_status_audits (
    id bigint NOT NULL,
    application_id bigint NOT NULL,
    changed_by_id bigint NOT NULL,
    from_status character varying(50),
    to_status character varying(50) NOT NULL,
    changed_at timestamp without time zone DEFAULT now() NOT NULL
);


ALTER TABLE public.job_application_status_audits OWNER TO postgres;

--
-- Name: job_application_status_audits_id_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.job_application_status_audits_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.job_application_status_audits_id_seq OWNER TO postgres;

--
-- Name: job_application_status_audits_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE public.job_application_status_audits_id_seq OWNED BY public.job_application_status_audits.id;


--
-- Name: job_applications; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.job_applications (
    id bigint NOT NULL,
    application_date timestamp(6) without time zone,
    status character varying(255),
    job_id bigint,
    user_id bigint,
    updated_at timestamp without time zone,
    cv_url character varying(1000),
    cv_file_name character varying(255),
    cv_id bigint,
    cover_letter text,
    cv_text_snapshot text,
    CONSTRAINT ck_job_applications_status CHECK (((status)::text = ANY ((ARRAY['submitted'::character varying, 'reviewing'::character varying, 'shortlisted'::character varying, 'interviewed'::character varying, 'offered'::character varying, 'hired'::character varying, 'rejected'::character varying, 'withdrawn'::character varying])::text[])))
);


ALTER TABLE public.job_applications OWNER TO postgres;

--
-- Name: job_applications_id_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

ALTER TABLE public.job_applications ALTER COLUMN id ADD GENERATED BY DEFAULT AS IDENTITY (
    SEQUENCE NAME public.job_applications_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1
);


--
-- Name: job_need_preferences; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.job_need_preferences (
    user_id bigint NOT NULL,
    desired_title character varying(255),
    desired_location character varying(255),
    desired_category character varying(255),
    desired_job_type character varying(120),
    desired_experience_level character varying(120),
    min_salary numeric(19,2),
    max_salary numeric(19,2),
    remote_only boolean DEFAULT false NOT NULL,
    created_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL,
    preferred_skills text,
    excluded_keywords text
);


ALTER TABLE public.job_need_preferences OWNER TO postgres;

--
-- Name: job_skills; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.job_skills (
    job_id bigint NOT NULL,
    skill_id bigint NOT NULL
);


ALTER TABLE public.job_skills OWNER TO postgres;

--
-- Name: jobs; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.jobs (
    id bigint NOT NULL,
    application_deadline timestamp(6) without time zone,
    description character varying(255),
    experience_level character varying(255),
    job_type character varying(255),
    location character varying(255),
    posted_date timestamp(6) without time zone,
    salary numeric(38,2),
    title character varying(255),
    company_id bigint,
    status character varying(50) NOT NULL,
    salary_min numeric,
    salary_max numeric,
    currency character varying(10) NOT NULL,
    updated_at timestamp without time zone,
    deleted_at timestamp without time zone,
    category character varying(120),
    image_url text,
    CONSTRAINT ck_jobs_salary_range CHECK (((salary_min IS NULL) OR (salary_max IS NULL) OR (salary_min <= salary_max))),
    CONSTRAINT ck_jobs_status CHECK (((status)::text = ANY ((ARRAY['draft'::character varying, 'open'::character varying, 'closed'::character varying, 'archived'::character varying])::text[])))
);


ALTER TABLE public.jobs OWNER TO postgres;

--
-- Name: jobs_id_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

ALTER TABLE public.jobs ALTER COLUMN id ADD GENERATED BY DEFAULT AS IDENTITY (
    SEQUENCE NAME public.jobs_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1
);


--
-- Name: message_attachments; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.message_attachments (
    id bigint NOT NULL,
    message_id bigint NOT NULL,
    file_name character varying(255),
    file_url text NOT NULL,
    public_id character varying(255),
    mime_type character varying(255),
    file_size bigint,
    created_at timestamp without time zone NOT NULL
);


ALTER TABLE public.message_attachments OWNER TO postgres;

--
-- Name: message_attachments_id_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.message_attachments_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.message_attachments_id_seq OWNER TO postgres;

--
-- Name: message_attachments_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE public.message_attachments_id_seq OWNED BY public.message_attachments.id;


--
-- Name: messages; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.messages (
    id bigint NOT NULL,
    conversation_id bigint NOT NULL,
    sender_id bigint NOT NULL,
    content text NOT NULL,
    type character varying(50) DEFAULT 'text'::character varying NOT NULL,
    created_at timestamp without time zone DEFAULT now() NOT NULL
);


ALTER TABLE public.messages OWNER TO postgres;

--
-- Name: messages_id_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.messages_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.messages_id_seq OWNER TO postgres;

--
-- Name: messages_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE public.messages_id_seq OWNED BY public.messages.id;


--
-- Name: notification_preferences; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.notification_preferences (
    user_id bigint NOT NULL,
    in_app_enabled boolean DEFAULT true NOT NULL,
    email_enabled boolean DEFAULT false NOT NULL,
    created_at timestamp without time zone DEFAULT now() NOT NULL
);


ALTER TABLE public.notification_preferences OWNER TO postgres;

--
-- Name: notifications; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.notifications (
    id bigint NOT NULL,
    user_id bigint NOT NULL,
    title character varying(255),
    content text,
    type character varying(100),
    is_read boolean DEFAULT false NOT NULL,
    created_at timestamp without time zone DEFAULT now() NOT NULL,
    target_url character varying(500)
);


ALTER TABLE public.notifications OWNER TO postgres;

--
-- Name: notifications_id_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.notifications_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.notifications_id_seq OWNER TO postgres;

--
-- Name: notifications_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE public.notifications_id_seq OWNED BY public.notifications.id;


--
-- Name: recruiter_activity_logs; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.recruiter_activity_logs (
    id bigint NOT NULL,
    actor_id bigint NOT NULL,
    company_id bigint,
    job_id bigint,
    application_id bigint,
    action_type character varying(60) NOT NULL,
    title character varying(255) NOT NULL,
    details character varying(1000),
    created_at timestamp without time zone DEFAULT now() NOT NULL
);


ALTER TABLE public.recruiter_activity_logs OWNER TO postgres;

--
-- Name: recruiter_activity_logs_id_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.recruiter_activity_logs_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.recruiter_activity_logs_id_seq OWNER TO postgres;

--
-- Name: recruiter_activity_logs_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE public.recruiter_activity_logs_id_seq OWNED BY public.recruiter_activity_logs.id;


--
-- Name: recruitment_campaign_applications; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.recruitment_campaign_applications (
    campaign_id bigint NOT NULL,
    application_id bigint NOT NULL
);


ALTER TABLE public.recruitment_campaign_applications OWNER TO postgres;

--
-- Name: recruitment_campaign_jobs; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.recruitment_campaign_jobs (
    campaign_id bigint NOT NULL,
    job_id bigint NOT NULL
);


ALTER TABLE public.recruitment_campaign_jobs OWNER TO postgres;

--
-- Name: recruitment_campaigns; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.recruitment_campaigns (
    id bigint NOT NULL,
    company_id bigint NOT NULL,
    created_by_id bigint NOT NULL,
    name character varying(255) NOT NULL,
    description text,
    status character varying(50) DEFAULT 'active'::character varying NOT NULL,
    target_hires integer,
    starts_at timestamp without time zone,
    ends_at timestamp without time zone,
    created_at timestamp without time zone NOT NULL,
    updated_at timestamp without time zone NOT NULL
);


ALTER TABLE public.recruitment_campaigns OWNER TO postgres;

--
-- Name: recruitment_campaigns_id_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.recruitment_campaigns_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.recruitment_campaigns_id_seq OWNER TO postgres;

--
-- Name: recruitment_campaigns_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE public.recruitment_campaigns_id_seq OWNED BY public.recruitment_campaigns.id;


--
-- Name: recruitment_events; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.recruitment_events (
    id bigint NOT NULL,
    company_id bigint,
    job_id bigint,
    application_id bigint,
    actor_id bigint,
    event_type character varying(100) NOT NULL,
    created_at timestamp without time zone NOT NULL,
    metadata text
);


ALTER TABLE public.recruitment_events OWNER TO postgres;

--
-- Name: recruitment_events_id_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.recruitment_events_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.recruitment_events_id_seq OWNER TO postgres;

--
-- Name: recruitment_events_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE public.recruitment_events_id_seq OWNED BY public.recruitment_events.id;


--
-- Name: saved_jobs; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.saved_jobs (
    id bigint NOT NULL,
    user_id bigint NOT NULL,
    job_id bigint NOT NULL,
    saved_at timestamp without time zone DEFAULT now() NOT NULL,
    note text,
    tag character varying(100)
);


ALTER TABLE public.saved_jobs OWNER TO postgres;

--
-- Name: saved_jobs_id_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.saved_jobs_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.saved_jobs_id_seq OWNER TO postgres;

--
-- Name: saved_jobs_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE public.saved_jobs_id_seq OWNED BY public.saved_jobs.id;


--
-- Name: skills; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.skills (
    id bigint NOT NULL,
    name character varying(255)
);


ALTER TABLE public.skills OWNER TO postgres;

--
-- Name: skills_id_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

ALTER TABLE public.skills ALTER COLUMN id ADD GENERATED BY DEFAULT AS IDENTITY (
    SEQUENCE NAME public.skills_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1
);


--
-- Name: user_cvs; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.user_cvs (
    id bigint NOT NULL,
    user_id bigint NOT NULL,
    cv_url character varying(1000) NOT NULL,
    file_name character varying(255),
    uploaded_at timestamp without time zone DEFAULT now()
);


ALTER TABLE public.user_cvs OWNER TO postgres;

--
-- Name: user_cvs_id_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.user_cvs_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.user_cvs_id_seq OWNER TO postgres;

--
-- Name: user_cvs_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE public.user_cvs_id_seq OWNED BY public.user_cvs.id;


--
-- Name: user_skills; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.user_skills (
    user_id bigint NOT NULL,
    skill_id bigint NOT NULL
);


ALTER TABLE public.user_skills OWNER TO postgres;

--
-- Name: user_tool_sessions; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.user_tool_sessions (
    id bigint NOT NULL,
    user_id bigint NOT NULL,
    tool_slug character varying(60) NOT NULL,
    input_json text NOT NULL,
    result_json text NOT NULL,
    created_at timestamp without time zone DEFAULT now() NOT NULL
);


ALTER TABLE public.user_tool_sessions OWNER TO postgres;

--
-- Name: user_tool_sessions_id_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.user_tool_sessions_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.user_tool_sessions_id_seq OWNER TO postgres;

--
-- Name: user_tool_sessions_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE public.user_tool_sessions_id_seq OWNED BY public.user_tool_sessions.id;


--
-- Name: users; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.users (
    id bigint NOT NULL,
    email character varying(255),
    name character varying(255),
    password_hash character varying(255),
    role character varying(255) DEFAULT 'CANDIDATE'::character varying NOT NULL,
    address character varying(255),
    avatar_url character varying(255),
    created_at timestamp(6) without time zone,
    cv_url character varying(255),
    experience_years integer,
    phone character varying(255),
    updated_at timestamp(6) without time zone,
    cv_text text,
    cv_role character varying(255),
    cv_objective text,
    cv_experience_highlights text,
    primary_cv_type character varying(50),
    mbti_type character varying(4),
    mbti_taken_at timestamp without time zone,
    mi_scores_json text,
    mi_taken_at timestamp without time zone,
    personality_public boolean DEFAULT false,
    CONSTRAINT ck_users_role CHECK (((role)::text = ANY ((ARRAY['CANDIDATE'::character varying, 'RECRUITER'::character varying, 'ADMIN'::character varying])::text[])))
);


ALTER TABLE public.users OWNER TO postgres;

--
-- Name: users_id_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

ALTER TABLE public.users ALTER COLUMN id ADD GENERATED BY DEFAULT AS IDENTITY (
    SEQUENCE NAME public.users_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1
);


--
-- Name: application_ai_scores id; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.application_ai_scores ALTER COLUMN id SET DEFAULT nextval('public.application_ai_scores_id_seq'::regclass);


--
-- Name: candidate_job_matches id; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.candidate_job_matches ALTER COLUMN id SET DEFAULT nextval('public.candidate_job_matches_id_seq'::regclass);


--
-- Name: career_guide_articles id; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.career_guide_articles ALTER COLUMN id SET DEFAULT nextval('public.career_guide_articles_id_seq'::regclass);


--
-- Name: company_follows id; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.company_follows ALTER COLUMN id SET DEFAULT nextval('public.company_follows_id_seq'::regclass);


--
-- Name: company_members id; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.company_members ALTER COLUMN id SET DEFAULT nextval('public.company_members_id_seq'::regclass);


--
-- Name: company_reviews id; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.company_reviews ALTER COLUMN id SET DEFAULT nextval('public.company_reviews_id_seq'::regclass);


--
-- Name: conversations id; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.conversations ALTER COLUMN id SET DEFAULT nextval('public.conversations_id_seq'::regclass);


--
-- Name: email_change_verifications id; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.email_change_verifications ALTER COLUMN id SET DEFAULT nextval('public.email_change_verifications_id_seq'::regclass);


--
-- Name: interview_schedules id; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.interview_schedules ALTER COLUMN id SET DEFAULT nextval('public.interview_schedules_id_seq'::regclass);


--
-- Name: job_alert_history id; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.job_alert_history ALTER COLUMN id SET DEFAULT nextval('public.job_alert_history_id_seq'::regclass);


--
-- Name: job_application_status_audits id; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.job_application_status_audits ALTER COLUMN id SET DEFAULT nextval('public.job_application_status_audits_id_seq'::regclass);


--
-- Name: message_attachments id; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.message_attachments ALTER COLUMN id SET DEFAULT nextval('public.message_attachments_id_seq'::regclass);


--
-- Name: messages id; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.messages ALTER COLUMN id SET DEFAULT nextval('public.messages_id_seq'::regclass);


--
-- Name: notifications id; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.notifications ALTER COLUMN id SET DEFAULT nextval('public.notifications_id_seq'::regclass);


--
-- Name: recruiter_activity_logs id; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.recruiter_activity_logs ALTER COLUMN id SET DEFAULT nextval('public.recruiter_activity_logs_id_seq'::regclass);


--
-- Name: recruitment_campaigns id; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.recruitment_campaigns ALTER COLUMN id SET DEFAULT nextval('public.recruitment_campaigns_id_seq'::regclass);


--
-- Name: recruitment_events id; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.recruitment_events ALTER COLUMN id SET DEFAULT nextval('public.recruitment_events_id_seq'::regclass);


--
-- Name: saved_jobs id; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.saved_jobs ALTER COLUMN id SET DEFAULT nextval('public.saved_jobs_id_seq'::regclass);


--
-- Name: user_cvs id; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.user_cvs ALTER COLUMN id SET DEFAULT nextval('public.user_cvs_id_seq'::regclass);


--
-- Name: user_tool_sessions id; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.user_tool_sessions ALTER COLUMN id SET DEFAULT nextval('public.user_tool_sessions_id_seq'::regclass);


--
-- Data for Name: application_ai_scores; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.application_ai_scores (id, application_id, score, level, raw_score, signals, cv_hash, job_hash, scored_at, pros, cons) FROM stdin;
3	1	48	weak_match	0.5332009196281433	Semantic CV-JD match	8f6e686c61ea6f79c1369dea2730e624d3c4ebd199869597163ab88fbf474a65	796004530288cfd278f2fc9b14c6e1d6b22693079d9f6cbf121661118ffc6845	2026-05-04 09:20:40.160709	\N	\N
5	3	42	weak_match	0.4612846076488495	Semantic CV-JD match	8f6e686c61ea6f79c1369dea2730e624d3c4ebd199869597163ab88fbf474a65	2c028e609ec9684a794fa3ae76ee2b781752431eac592c1c52b98a8884f5742f	2026-05-04 09:20:40.423036	\N	\N
6	4	42	weak_match	0.4612846076488495	Semantic CV-JD match	8f6e686c61ea6f79c1369dea2730e624d3c4ebd199869597163ab88fbf474a65	2c028e609ec9684a794fa3ae76ee2b781752431eac592c1c52b98a8884f5742f	2026-05-04 09:20:40.680782	\N	\N
7	5	50	weak_match	0.5548248887062073	Semantic CV-JD match	8f6e686c61ea6f79c1369dea2730e624d3c4ebd199869597163ab88fbf474a65	9b16f05cbdf0d0a62ea234c3ee0f6aec0a787c35e57cfd4c5bc1c5fff2e60687	2026-05-04 09:20:40.935841	\N	\N
2	6	48	weak_match	0.5332009196281433	Semantic CV-JD match	8f6e686c61ea6f79c1369dea2730e624d3c4ebd199869597163ab88fbf474a65	796004530288cfd278f2fc9b14c6e1d6b22693079d9f6cbf121661118ffc6845	2026-05-04 09:20:41.19479	\N	\N
1	8	48	weak_match	0.5362358093261719	Semantic CV-JD match	8f6e686c61ea6f79c1369dea2730e624d3c4ebd199869597163ab88fbf474a65	c0ac187a512ed44a296a8869c00f692243312a9a5ef28263b2ce3f533395d35e	2026-05-04 09:20:41.45677	\N	\N
8	7	42	weak_match	0.46269065141677856	Semantic CV-JD match	e6fa9d55cd60847c5e09a5b2df1e8e4cfb77a045838698b404199346128626c0	796004530288cfd278f2fc9b14c6e1d6b22693079d9f6cbf121661118ffc6845	2026-05-04 09:20:41.732761	\N	\N
4	2	50	weak_match	0.5548248887062073	Semantic CV-JD match	8f6e686c61ea6f79c1369dea2730e624d3c4ebd199869597163ab88fbf474a65	9b16f05cbdf0d0a62ea234c3ee0f6aec0a787c35e57cfd4c5bc1c5fff2e60687	2026-05-04 09:20:42.001413	\N	\N
10	10	31	weak_match	0.3421647250652313	Semantic CV-JD match	28ebd31bcd6afab621b3abb4f596cfe1c7ed3686475e91838ef4e20bc76c5ee6	40a32a2d9a4636c3e38a401825a2c11aa2076e8981b7e5196ac390cf5923a76f	2026-05-04 09:20:42.106659	\N	\N
9	9	65	possible_match	0.7227869629859924	Semantic CV-JD match	28ebd31bcd6afab621b3abb4f596cfe1c7ed3686475e91838ef4e20bc76c5ee6	0fac2dfab7268b1f5819f7c760dda95ba396dfeec3e624550cff0f063c5f7cfd	2026-05-04 09:21:49.015408	\N	\N
\.


--
-- Data for Name: candidate_job_matches; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.candidate_job_matches (id, user_id, job_id, preference_updated_at, score, reasons, created_at) FROM stdin;
1	6	2	2026-04-29 19:56:43.681517	77	AI matched job context\nShared important terms	2026-05-03 14:48:39.851851
2	6	5	2026-04-29 19:56:43.681517	71	AI matched job context\nShared important terms	2026-05-03 14:48:39.851851
3	6	11	2026-04-29 19:56:43.681517	71	AI matched job context\nShared important terms	2026-05-03 14:48:39.851851
4	6	125	2026-04-29 19:56:43.681517	71	AI matched job context\nShared important terms	2026-05-03 14:48:39.851851
5	6	131	2026-04-29 19:56:43.681517	71	AI matched job context\nShared important terms	2026-05-03 14:48:39.851851
6	6	7	2026-04-29 19:56:43.681517	71	AI matched job context\nShared important terms	2026-05-03 14:48:39.851851
7	6	13	2026-04-29 19:56:43.681517	71	AI matched job context\nShared important terms	2026-05-03 14:48:39.851851
8	6	127	2026-04-29 19:56:43.681517	71	AI matched job context\nShared important terms	2026-05-03 14:48:39.851851
9	6	133	2026-04-29 19:56:43.681517	71	AI matched job context\nShared important terms	2026-05-03 14:48:39.851851
10	6	9	2026-04-29 19:56:43.681517	71	AI matched job context\nShared important terms	2026-05-03 14:48:39.851851
11	6	129	2026-04-29 19:56:43.681517	71	AI matched job context\nShared important terms	2026-05-03 14:48:39.851851
12	6	68	2026-04-29 19:56:43.681517	67	AI matched job context\nShared important terms	2026-05-03 14:48:39.851851
13	6	74	2026-04-29 19:56:43.681517	67	AI matched job context\nShared important terms	2026-05-03 14:48:39.851851
14	6	70	2026-04-29 19:56:43.681517	67	AI matched job context\nShared important terms	2026-05-03 14:48:39.851851
15	6	66	2026-04-29 19:56:43.681517	67	AI matched job context\nShared important terms	2026-05-03 14:48:39.851851
16	6	72	2026-04-29 19:56:43.681517	67	AI matched job context\nShared important terms	2026-05-03 14:48:39.851851
17	6	4	2026-04-29 19:56:43.681517	61	AI matched job context\nShared important terms	2026-05-03 14:48:39.851851
18	6	108	2026-04-29 19:56:43.681517	43	AI matched job context\nShared important terms	2026-05-03 14:48:39.851851
19	6	114	2026-04-29 19:56:43.681517	43	AI matched job context\nShared important terms	2026-05-03 14:48:39.851851
20	6	110	2026-04-29 19:56:43.681517	43	AI matched job context\nShared important terms	2026-05-03 14:48:39.851851
21	6	106	2026-04-29 19:56:43.681517	43	AI matched job context\nShared important terms	2026-05-03 14:48:39.851851
22	6	112	2026-04-29 19:56:43.681517	43	AI matched job context\nShared important terms	2026-05-03 14:48:39.851851
23	6	10	2026-04-29 19:56:43.681517	41	AI matched job context\nShared important terms	2026-05-03 14:48:39.851851
24	6	12	2026-04-29 19:56:43.681517	41	AI matched job context\nShared important terms	2026-05-03 14:48:39.851851
25	6	8	2026-04-29 19:56:43.681517	40	AI matched job context\nShared important terms	2026-05-03 14:48:39.851851
26	6	14	2026-04-29 19:56:43.681517	40	AI matched job context\nShared important terms	2026-05-03 14:48:39.851851
27	6	6	2026-04-29 19:56:43.681517	40	AI matched job context\nShared important terms	2026-05-03 14:48:39.851851
28	6	226	2026-04-29 19:56:43.681517	39	AI matched job context\nShared important terms	2026-05-03 14:48:39.851851
29	6	232	2026-04-29 19:56:43.681517	39	AI matched job context\nShared important terms	2026-05-03 14:48:39.851851
30	6	128	2026-04-29 19:56:43.681517	39	AI matched job context\nShared important terms	2026-05-03 14:48:39.851851
31	177	2	2026-05-02 19:29:30.315546	78	AI matched job context\nShared important terms	2026-05-04 07:29:43.64795
32	177	5	2026-05-02 19:29:30.315546	71	AI matched job context\nShared important terms	2026-05-04 07:29:43.64795
33	177	11	2026-05-02 19:29:30.315546	71	AI matched job context\nShared important terms	2026-05-04 07:29:43.648481
34	177	125	2026-05-02 19:29:30.315546	71	AI matched job context\nShared important terms	2026-05-04 07:29:43.648481
35	177	131	2026-05-02 19:29:30.315546	71	AI matched job context\nShared important terms	2026-05-04 07:29:43.648481
36	177	7	2026-05-02 19:29:30.315546	71	AI matched job context\nShared important terms	2026-05-04 07:29:43.648481
37	177	13	2026-05-02 19:29:30.315546	71	AI matched job context\nShared important terms	2026-05-04 07:29:43.648481
38	177	127	2026-05-02 19:29:30.315546	71	AI matched job context\nShared important terms	2026-05-04 07:29:43.648481
39	177	133	2026-05-02 19:29:30.315546	71	AI matched job context\nShared important terms	2026-05-04 07:29:43.648481
40	177	9	2026-05-02 19:29:30.315546	71	AI matched job context\nShared important terms	2026-05-04 07:29:43.648481
41	177	129	2026-05-02 19:29:30.315546	71	AI matched job context\nShared important terms	2026-05-04 07:29:43.648481
42	177	68	2026-05-02 19:29:30.315546	67	AI matched job context\nShared important terms	2026-05-04 07:29:43.648481
43	177	74	2026-05-02 19:29:30.315546	67	AI matched job context\nShared important terms	2026-05-04 07:29:43.648481
44	177	70	2026-05-02 19:29:30.315546	67	AI matched job context\nShared important terms	2026-05-04 07:29:43.648481
45	177	66	2026-05-02 19:29:30.315546	67	AI matched job context\nShared important terms	2026-05-04 07:29:43.648481
46	177	72	2026-05-02 19:29:30.315546	67	AI matched job context\nShared important terms	2026-05-04 07:29:43.648481
47	177	4	2026-05-02 19:29:30.315546	61	AI matched job context\nShared important terms	2026-05-04 07:29:43.648481
48	177	108	2026-05-02 19:29:30.315546	43	AI matched job context\nShared important terms	2026-05-04 07:29:43.648481
49	177	114	2026-05-02 19:29:30.315546	43	AI matched job context\nShared important terms	2026-05-04 07:29:43.648481
50	177	110	2026-05-02 19:29:30.315546	43	AI matched job context\nShared important terms	2026-05-04 07:29:43.648481
51	177	106	2026-05-02 19:29:30.315546	43	AI matched job context\nShared important terms	2026-05-04 07:29:43.648481
52	177	112	2026-05-02 19:29:30.315546	43	AI matched job context\nShared important terms	2026-05-04 07:29:43.648481
53	177	8	2026-05-02 19:29:30.315546	40	AI matched job context\nShared important terms	2026-05-04 07:29:43.648481
54	177	14	2026-05-02 19:29:30.315546	40	AI matched job context\nShared important terms	2026-05-04 07:29:43.648481
55	177	10	2026-05-02 19:29:30.315546	40	AI matched job context\nShared important terms	2026-05-04 07:29:43.648481
56	177	6	2026-05-02 19:29:30.315546	40	AI matched job context\nShared important terms	2026-05-04 07:29:43.648481
57	177	12	2026-05-02 19:29:30.315546	40	AI matched job context\nShared important terms	2026-05-04 07:29:43.648481
58	177	226	2026-05-02 19:29:30.315546	39	AI matched job context\nShared important terms	2026-05-04 07:29:43.648481
59	177	232	2026-05-02 19:29:30.315546	39	AI matched job context\nShared important terms	2026-05-04 07:29:43.648481
60	177	128	2026-05-02 19:29:30.315546	39	AI matched job context\nShared important terms	2026-05-04 07:29:43.648481
\.


--
-- Data for Name: career_guide_articles; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.career_guide_articles (id, slug, title, summary, content, category, cover_image_url, reading_time_minutes, featured, published_at, created_at, updated_at) FROM stdin;
1	cv-thuyet-phuc-nha-tuyen-dung	Cách viết CV thuyết phục nhà tuyển dụng	Cách sắp xếp CV, chọn từ khóa và trình bày thành tựu để hồ sơ dễ đi qua vòng sàng lọc đầu tiên.	Chia CV thành 4 phần rõ ràng: thông tin cá nhân, mục tiêu nghề nghiệp, kinh nghiệm và kỹ năng.\\n\\nĐầu mỗi mô tả công việc, hãy nêu kết quả đo được thay vì chỉ liệt kê nhiệm vụ. Ví dụ: tăng doanh số, giảm thời gian xử lý, cải thiện tỉ lệ phản hồi.\\n\\nKhi ứng tuyển online, đồng bộ từ khóa trong mô tả với JD để hệ thống ATS dễ nhận diện hơn.	Tìm việc	https://images.unsplash.com/photo-1455390582262-044cdead277a?auto=format&fit=crop&w=1200&q=80	6	t	2026-04-24 12:27:23.966809	2026-04-24 12:27:23.966809	2026-04-24 12:27:23.966809
2	phong-van-hieu-qua	Chuẩn bị phỏng vấn hiệu quả trong 48 giờ	Lộ trình ôn tập nhanh để bạn trả lời gọn, đúng trọng tâm và tạo ấn tượng chuyên nghiệp.	Trong 24 giờ đầu, hãy đọc lại JD, nghiên cứu công ty và liệt kê 5 câu hỏi phổ biến nhất cho vị trí.\\n\\nNgày kế tiếp, luyện trả lời theo cấu trúc ngắn: bối cảnh, hành động, kết quả. Với câu hỏi khó, đừng vòng vo; hãy nói rõ điều bạn làm được và điều bạn còn muốn học.\\n\\nKết thúc buổi chuẩn bị bằng việc kiểm tra trang phục, đường đi và thiết bị nếu phỏng vấn online.	Phỏng vấn	https://images.unsplash.com/photo-1522202176988-66273c2fd55f?auto=format&fit=crop&w=1200&q=80	5	t	2026-04-24 12:27:23.966809	2026-04-24 12:27:23.966809	2026-04-24 12:27:23.966809
3	chuyen-nganh-sang-it	Chuyển ngành sang IT bắt đầu từ đâu	Bản đồ kỹ năng, lộ trình học và cách viết lại hồ sơ khi bạn chuyển từ ngành khác sang công nghệ.	Bắt đầu từ một vai trò cụ thể thay vì nói chung chung về ngành IT. Frontend, backend, QA, data hay product đều có bộ kỹ năng khác nhau.\\n\\nHãy chọn một ngôn ngữ hoặc công cụ chính, làm 2 đến 3 dự án nhỏ, rồi viết lại CV theo hướng thể hiện khả năng giải quyết vấn đề.\\n\\nKhi chưa có kinh nghiệm chính thức, portfolio và mô tả dự án thực tế quan trọng hơn các danh sách khóa học.	Chuyển ngành	https://images.unsplash.com/photo-1516321318423-f06f85e504b3?auto=format&fit=crop&w=1200&q=80	7	f	2026-04-24 12:27:23.966809	2026-04-24 12:27:23.966809	2026-04-24 12:27:23.966809
4	deal-luong-dung-cach	Deal lương đúng cách mà không mất lợi thế	Một số nguyên tắc giúp bạn thương lượng lương, thưởng và phúc lợi rõ ràng hơn trong vòng offer.	Khi nhận offer, đừng phản hồi ngay nếu bạn chưa nắm đủ dữ liệu. Hãy hỏi về lương cứng, thưởng, thời gian thử việc, phụ cấp và cơ hội review sau 3 đến 6 tháng.\\n\\nNếu muốn thương lượng, hãy đưa ra lý do dựa trên giá trị bạn mang lại: kinh nghiệm, kỹ năng hiếm, phạm vi trách nhiệm hoặc benchmark thị trường.\\n\\nMục tiêu không phải là "đòi thêm", mà là chốt một gói phù hợp với đóng góp và kỳ vọng của cả hai bên.	Lương thưởng	https://images.unsplash.com/photo-1554224155-6726b3ff858f?auto=format&fit=crop&w=1200&q=80	4	f	2026-04-24 12:27:23.966809	2026-04-24 12:27:23.966809	2026-04-24 12:27:23.966809
\.


--
-- Data for Name: companies; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.companies (id, description, industry, location, name, website, created_at, logo_url, created_by, updated_at, deleted_at) FROM stdin;
1	Công ty phát triển phần mềm và nền tảng tuyển dụng	Công nghệ	Hà Nội	Công ty TNHH Công nghệ	https://www.example.com	\N	\N	\N	2026-04-03 09:57:38.131213	\N
2	Công ty phần mềm hàng đầu Việt Nam	IT	Đà Nẵng	FPT Software	https://fpt.com	2026-03-31 10:00:00	https://logo.com/fpt.png	\N	2026-03-31 10:00:00	\N
3	\N	\N	\N	Test Company 15251178880900	\N	\N	\N	9	2026-04-03 09:57:38.131213	\N
4	\N	\N	\N	Test Company 15251270603500	\N	\N	\N	14	2026-04-03 09:57:38.131213	\N
5	\N	\N	\N	company-60346663766400	\N	2026-04-03 10:24:04.987622	\N	41	2026-04-03 10:24:04.987622	\N
6	\N	\N	\N	company-60346822867400	\N	2026-04-03 10:24:05.142667	\N	44	2026-04-03 10:24:05.142667	\N
7	\N	\N	\N	company-60346873328600	\N	2026-04-03 10:24:05.196575	\N	45	2026-04-03 10:24:05.196575	\N
8	\N	\N	\N	company-60404597439200	\N	2026-04-03 10:25:02.92119	\N	49	2026-04-03 10:25:02.92119	\N
9	\N	\N	\N	company-60404744015800	\N	2026-04-03 10:25:03.067216	\N	52	2026-04-03 10:25:03.067216	\N
10	\N	\N	\N	company-60404803515300	\N	2026-04-03 10:25:03.125304	\N	53	2026-04-03 10:25:03.125304	\N
11	\N	\N	\N	company-60443142905600	\N	2026-04-03 10:25:41.46769	\N	57	2026-04-03 10:25:41.46769	\N
12	\N	\N	\N	company-60443393934600	\N	2026-04-03 10:25:41.716427	\N	60	2026-04-03 10:25:41.716427	\N
13	\N	\N	\N	company-60443447992800	\N	2026-04-03 10:25:41.770398	\N	61	2026-04-03 10:25:41.770398	\N
14	\N	\N	\N	company-97022874727500	\N	2026-04-03 20:35:21.198087	\N	65	2026-04-03 20:35:21.198087	\N
15	\N	\N	\N	company-97023002502100	\N	2026-04-03 20:35:21.325637	\N	68	2026-04-03 20:35:21.325637	\N
16	\N	\N	\N	company-97023050157000	\N	2026-04-03 20:35:21.372565	\N	69	2026-04-03 20:35:21.372565	\N
17	\N	\N	\N	company-97055229084700	\N	2026-04-03 20:35:53.552328	\N	73	2026-04-03 20:35:53.552328	\N
18	\N	\N	\N	company-97055406049600	\N	2026-04-03 20:35:53.728948	\N	76	2026-04-03 20:35:53.728948	\N
19	\N	\N	\N	company-97055451329200	\N	2026-04-03 20:35:53.774495	\N	77	2026-04-03 20:35:53.774495	\N
20	\N	\N	\N	company-2572212312500	\N	2026-04-03 21:22:32.535853	\N	81	2026-04-03 21:22:32.535853	\N
21	\N	\N	\N	company-2572292160600	\N	2026-04-03 21:22:32.614855	\N	84	2026-04-03 21:22:32.614855	\N
22	\N	\N	\N	company-2572323218700	\N	2026-04-03 21:22:32.645829	\N	85	2026-04-03 21:22:32.645829	\N
23	\N	\N	\N	company-2597155365000	\N	2026-04-03 21:22:57.478861	\N	89	2026-04-03 21:22:57.478861	\N
24	\N	\N	\N	company-2597271453100	\N	2026-04-03 21:22:57.594621	\N	92	2026-04-03 21:22:57.594621	\N
25	\N	\N	\N	company-2597299952400	\N	2026-04-03 21:22:57.62308	\N	93	2026-04-03 21:22:57.62308	\N
26	\N	\N	\N	company-4074047980400	\N	2026-04-03 21:47:34.37132	\N	97	2026-04-03 21:47:34.37132	\N
27	\N	\N	\N	company-4074220790500	\N	2026-04-03 21:47:34.544307	\N	100	2026-04-03 21:47:34.544307	\N
28	\N	\N	\N	company-4074286630700	\N	2026-04-03 21:47:34.609995	\N	101	2026-04-03 21:47:34.609995	\N
29	\N	\N	\N	company-4117011513700	\N	2026-04-03 21:48:17.335151	\N	105	2026-04-03 21:48:17.335151	\N
30	\N	\N	\N	company-4117121731600	\N	2026-04-03 21:48:17.444253	\N	108	2026-04-03 21:48:17.444253	\N
31	\N	\N	\N	company-4117175805799	\N	2026-04-03 21:48:17.498644	\N	109	2026-04-03 21:48:17.498644	\N
32	\N	\N	\N	company-6066417534600	\N	2026-04-03 22:20:46.741382	\N	113	2026-04-03 22:20:46.741382	\N
33	\N	\N	\N	company-6066624275800	\N	2026-04-03 22:20:46.947781	\N	116	2026-04-03 22:20:46.947781	\N
34	\N	\N	\N	company-6066692094200	\N	2026-04-03 22:20:47.01472	\N	117	2026-04-03 22:20:47.01472	\N
35	\N	\N	\N	company-11365354769000	\N	2026-04-03 23:49:05.677223	\N	121	2026-04-03 23:49:05.677223	\N
36	\N	\N	\N	company-11365475431100	\N	2026-04-03 23:49:05.798201	\N	124	2026-04-03 23:49:05.798201	\N
37	\N	\N	\N	company-11365566147300	\N	2026-04-03 23:49:05.889216	\N	125	2026-04-03 23:49:05.889216	\N
38	\N	\N	\N	company-11401986576100	\N	2026-04-03 23:49:42.311438	\N	129	2026-04-03 23:49:42.311438	\N
39	\N	\N	\N	company-11402235394500	\N	2026-04-03 23:49:42.558612	\N	132	2026-04-03 23:49:42.558612	\N
40	\N	\N	\N	company-11402307981500	\N	2026-04-03 23:49:42.631361	\N	133	2026-04-03 23:49:42.631361	\N
41	\N	\N	\N	company-82996362358500	\N	2026-04-04 19:42:59.202673	\N	137	2026-04-04 19:42:59.202673	\N
42	\N	\N	\N	company-82996521484600	\N	2026-04-04 19:42:59.360044	\N	140	2026-04-04 19:42:59.360044	\N
43	\N	\N	\N	company-82996623959000	\N	2026-04-04 19:42:59.462457	\N	141	2026-04-04 19:42:59.462457	\N
44	\N	\N	\N	company-139606877805300	\N	2026-04-05 11:26:30.97461	\N	145	2026-04-05 11:26:30.97461	\N
45	\N	\N	\N	company-139607015600499	\N	2026-04-05 11:26:31.112163	\N	148	2026-04-05 11:26:31.112163	\N
46	\N	\N	\N	company-139607111378100	\N	2026-04-05 11:26:31.207471	\N	149	2026-04-05 11:26:31.207471	\N
47	\N	\N	\N	company-142768885761600	\N	2026-04-05 12:19:12.982457	\N	153	2026-04-05 12:19:12.982457	\N
48	\N	\N	\N	company-142769068014400	\N	2026-04-05 12:19:13.164323	\N	156	2026-04-05 12:19:13.164323	\N
49	\N	\N	\N	company-142769319420000	\N	2026-04-05 12:19:13.415894	\N	157	2026-04-05 12:19:13.415894	\N
50	\N	\N	\N	company-15424852454000	\N	2026-04-13 14:17:32.175853	\N	162	2026-04-13 14:17:32.175853	\N
51	\N	\N	\N	company-15424997622000	\N	2026-04-13 14:17:32.320764	\N	165	2026-04-13 14:17:32.320764	\N
52	\N	\N	\N	company-15425193081200	\N	2026-04-13 14:17:32.515894	\N	166	2026-04-13 14:17:32.515894	\N
53	\N	\N	\N	company-15560488083700	\N	2026-04-13 14:19:47.811703	\N	170	2026-04-13 14:19:47.811703	\N
54	\N	\N	\N	company-15560664501600	\N	2026-04-13 14:19:47.987181	\N	173	2026-04-13 14:19:47.987181	\N
55	\N	\N	\N	company-15560891913100	\N	2026-04-13 14:19:48.214954	\N	174	2026-04-13 14:19:48.214954	\N
56	\N	\N	\N	company-19655383299700	\N	2026-04-24 12:19:57.708041	\N	180	2026-04-24 12:19:57.708041	\N
57	\N	\N	\N	company-19655565129400	\N	2026-04-24 12:19:57.889688	\N	183	2026-04-24 12:19:57.889688	\N
58	\N	\N	\N	company-19655759289100	\N	2026-04-24 12:19:58.082949	\N	184	2026-04-24 12:19:58.082949	\N
59	\N	\N	\N	company-19744215726900	\N	2026-04-24 12:21:26.536366	\N	188	2026-04-24 12:21:26.536366	\N
60	\N	\N	\N	company-19744407004100	\N	2026-04-24 12:21:26.730699	\N	191	2026-04-24 12:21:26.730699	\N
61	\N	\N	\N	company-19744527721300	\N	2026-04-24 12:21:26.851403	\N	192	2026-04-24 12:21:26.851403	\N
62	\N	\N	\N	company-19804133832100	\N	2026-04-24 12:22:26.458719	\N	196	2026-04-24 12:22:26.458719	\N
63	\N	\N	\N	company-19804260935900	\N	2026-04-24 12:22:26.585172	\N	199	2026-04-24 12:22:26.585172	\N
64	\N	\N	\N	company-19804375696900	\N	2026-04-24 12:22:26.700361	\N	200	2026-04-24 12:22:26.700361	\N
65	\N	\N	\N	company-20286661819800	\N	2026-04-24 12:30:28.9874	\N	204	2026-04-24 12:30:28.9874	\N
66	\N	\N	\N	company-20286856935500	\N	2026-04-24 12:30:29.181445	\N	207	2026-04-24 12:30:29.181445	\N
67	\N	\N	\N	company-20286992531700	\N	2026-04-24 12:30:29.316668	\N	208	2026-04-24 12:30:29.316668	\N
68	\N	\N	\N	company-32751787173400	\N	2026-04-24 15:58:14.112971	\N	212	2026-04-24 15:58:14.112971	\N
69	\N	\N	\N	company-32752032401800	\N	2026-04-24 15:58:14.355928	\N	215	2026-04-24 15:58:14.355928	\N
70	\N	\N	\N	company-32752344300100	\N	2026-04-24 15:58:14.668585	\N	216	2026-04-24 15:58:14.668585	\N
71	\N	\N	\N	company-48023235448400	\N	2026-04-24 20:12:47.817291	\N	220	2026-04-24 20:12:47.817291	\N
72	\N	\N	\N	company-48023424202400	\N	2026-04-24 20:12:48.00537	\N	223	2026-04-24 20:12:48.00537	\N
73	\N	\N	\N	company-48023603232600	\N	2026-04-24 20:12:48.184264	\N	224	2026-04-24 20:12:48.184264	\N
74	\N	\N	\N	company-48266018758000	\N	2026-04-24 20:16:50.600332	\N	228	2026-04-24 20:16:50.600332	\N
75	\N	\N	\N	company-48266181625700	\N	2026-04-24 20:16:50.7624	\N	231	2026-04-24 20:16:50.7624	\N
76	\N	\N	\N	company-48266427562700	\N	2026-04-24 20:16:51.008438	\N	232	2026-04-24 20:16:51.008438	\N
77	\N	\N	\N	company-118133499849700	\N	2026-04-27 18:19:24.823116	\N	237	2026-04-27 18:19:24.823116	\N
78	\N	\N	\N	company-118133708823800	\N	2026-04-27 18:19:25.033652	\N	240	2026-04-27 18:19:25.033652	\N
79	\N	\N	\N	company-118133974202300	\N	2026-04-27 18:19:25.298001	\N	241	2026-04-27 18:19:25.298001	\N
80	\N	\N	\N	company-118270047078900	\N	2026-04-27 18:21:41.372144	\N	245	2026-04-27 18:21:41.372144	\N
81	\N	\N	\N	company-118270173776700	\N	2026-04-27 18:21:41.497816	\N	248	2026-04-27 18:21:41.497816	\N
82	\N	\N	\N	company-118270333879600	\N	2026-04-27 18:21:41.658816	\N	249	2026-04-27 18:21:41.658816	\N
83	\N	\N	\N	company-5678095183300	\N	2026-04-29 08:18:06.416833	\N	253	2026-04-29 08:18:06.416833	\N
84	\N	\N	\N	company-5678289469800	\N	2026-04-29 08:18:06.614297	\N	256	2026-04-29 08:18:06.614297	\N
85	\N	\N	\N	company-5678587418700	\N	2026-04-29 08:18:06.907815	\N	257	2026-04-29 08:18:06.907815	\N
86	\N	\N	\N	company-5830531016600	\N	2026-04-29 08:20:38.854591	\N	261	2026-04-29 08:20:38.854591	\N
87	\N	\N	\N	company-5830706753100	\N	2026-04-29 08:20:39.031756	\N	264	2026-04-29 08:20:39.031756	\N
88	\N	\N	\N	company-5830955776400	\N	2026-04-29 08:20:39.282054	\N	265	2026-04-29 08:20:39.282054	\N
89	\N	\N	\N	company-6100000972500	\N	2026-04-29 08:25:08.327342	\N	269	2026-04-29 08:25:08.327342	\N
90	\N	\N	\N	company-6100342043300	\N	2026-04-29 08:25:08.667037	\N	272	2026-04-29 08:25:08.667037	\N
91	\N	\N	\N	company-6100636002500	\N	2026-04-29 08:25:08.960689	\N	273	2026-04-29 08:25:08.960689	\N
92	\N	\N	\N	company-9524325730600	\N	2026-04-29 09:22:12.651205	\N	277	2026-04-29 09:22:12.651205	\N
93	\N	\N	\N	company-9524531460000	\N	2026-04-29 09:22:12.85637	\N	280	2026-04-29 09:22:12.85637	\N
94	\N	\N	\N	company-9524930280300	\N	2026-04-29 09:22:13.254901	\N	281	2026-04-29 09:22:13.254901	\N
95	\N	\N	\N	company-12290888427500	\N	2026-04-29 10:08:19.214062	\N	285	2026-04-29 10:08:19.214062	\N
96	\N	\N	\N	company-12291060798800	\N	2026-04-29 10:08:19.385279	\N	288	2026-04-29 10:08:19.385279	\N
97	\N	\N	\N	company-12291250873700	\N	2026-04-29 10:08:19.576276	\N	289	2026-04-29 10:08:19.576276	\N
98	\N	\N	\N	company-29619903551100	\N	2026-04-29 14:57:08.228859	\N	293	2026-04-29 14:57:08.228859	\N
99	\N	\N	\N	company-29620065507000	\N	2026-04-29 14:57:08.389275	\N	296	2026-04-29 14:57:08.389275	\N
100	\N	\N	\N	company-29620228413100	\N	2026-04-29 14:57:08.55309	\N	297	2026-04-29 14:57:08.55309	\N
101	\N	\N	\N	company-29759692103100	\N	2026-04-29 14:59:28.017974	\N	301	2026-04-29 14:59:28.017974	\N
102	\N	\N	\N	company-29759888643400	\N	2026-04-29 14:59:28.21303	\N	304	2026-04-29 14:59:28.21303	\N
103	\N	\N	\N	company-29760119309400	\N	2026-04-29 14:59:28.444109	\N	305	2026-04-29 14:59:28.444109	\N
104	\N	\N	\N	company-29868745274700	\N	2026-04-29 15:01:17.070827	\N	309	2026-04-29 15:01:17.070827	\N
105	\N	\N	\N	company-29869034581100	\N	2026-04-29 15:01:17.359537	\N	312	2026-04-29 15:01:17.359537	\N
106	\N	\N	\N	company-29869248777900	\N	2026-04-29 15:01:17.573938	\N	313	2026-04-29 15:01:17.573938	\N
107	Demo company for TTJobs sample data in Information Technology.	Information Technology	Ha Noi	Astra Tech Labs	https://astra-tech.example.com	2026-04-29 15:16:50.235386	https://placehold.co/96x96/E0E7FF/2563EB?text=AT	\N	2026-04-29 15:16:50.235386	\N
108	Demo company for TTJobs sample data in Sales.	Sales	Ho Chi Minh City	BluePeak Commerce	https://bluepeak.example.com	2026-04-29 15:16:50.235386	https://placehold.co/96x96/DBEAFE/1D4ED8?text=BP	\N	2026-04-29 15:16:50.235386	\N
109	Demo company for TTJobs sample data in Marketing.	Marketing	Da Nang	Nova Marketing House	https://nova-marketing.example.com	2026-04-29 15:16:50.235386	https://placehold.co/96x96/FCE7F3/BE185D?text=NM	\N	2026-04-29 15:16:50.235386	\N
110	Demo company for TTJobs sample data in Human Resources.	Human Resources	Ha Noi	PeopleFirst Group	https://peoplefirst.example.com	2026-04-29 15:16:50.235386	https://placehold.co/96x96/DCFCE7/15803D?text=PF	\N	2026-04-29 15:16:50.235386	\N
111	Demo company for TTJobs sample data in Finance.	Finance	Ho Chi Minh City	FinEdge Capital	https://finedge.example.com	2026-04-29 15:16:50.235386	https://placehold.co/96x96/FEF3C7/B45309?text=FE	\N	2026-04-29 15:16:50.235386	\N
112	Demo company for TTJobs sample data in Customer Service.	Customer Service	Remote	CareLine Services	https://careline.example.com	2026-04-29 15:16:50.235386	https://placehold.co/96x96/E0F2FE/0369A1?text=CL	\N	2026-04-29 15:16:50.235386	\N
113	Demo company for TTJobs sample data in Real Estate.	Real Estate	Ha Noi	UrbanNest Realty	https://urbannest.example.com	2026-04-29 15:16:50.235386	https://placehold.co/96x96/F3E8FF/7E22CE?text=UN	\N	2026-04-29 15:16:50.235386	\N
114	Demo company for TTJobs sample data in Accounting.	Accounting	Ho Chi Minh City	LedgerPro Advisors	https://ledgerpro.example.com	2026-04-29 15:16:50.235386	https://placehold.co/96x96/ECFCCB/4D7C0F?text=LP	\N	2026-04-29 15:16:50.235386	\N
115	Demo company for TTJobs sample data in Design.	Design	Da Nang	PixelCraft Studio	https://pixelcraft.example.com	2026-04-29 15:16:50.235386	https://placehold.co/96x96/FFE4E6/BE123C?text=PC	\N	2026-04-29 15:16:50.235386	\N
116	Demo company for TTJobs sample data in Business Development.	Business Development	Hybrid	GrowthForge Partners	https://growthforge.example.com	2026-04-29 15:16:50.235386	https://placehold.co/96x96/CCFBF1/0F766E?text=GF	\N	2026-04-29 15:16:50.235386	\N
117	Demo company for TTJobs sample data in Information Technology.	Information Technology	Ho Chi Minh City	CloudNexus Software	https://cloudnexus.example.com	2026-04-29 15:16:50.235386	https://placehold.co/96x96/E0E7FF/4338CA?text=CN	\N	2026-04-29 15:16:50.235386	\N
118	Demo company for TTJobs sample data in Sales.	Sales	Da Nang	MarketLane Retail	https://marketlane.example.com	2026-04-29 15:16:50.235386	https://placehold.co/96x96/DBEAFE/2563EB?text=ML	\N	2026-04-29 15:16:50.235386	\N
119	Demo company for TTJobs sample data in Marketing.	Marketing	Ha Noi	SignalWave Media	https://signalwave.example.com	2026-04-29 15:16:50.235386	https://placehold.co/96x96/FCE7F3/DB2777?text=SW	\N	2026-04-29 15:16:50.235386	\N
120	Demo company for TTJobs sample data in Human Resources.	Human Resources	Remote	TalentBridge Vietnam	https://talentbridge.example.com	2026-04-29 15:16:50.235386	https://placehold.co/96x96/DCFCE7/16A34A?text=TB	\N	2026-04-29 15:16:50.235386	\N
121	Demo company for TTJobs sample data in Finance.	Finance	Ha Noi	TrustBank Digital	https://trustbank.example.com	2026-04-29 15:16:50.235386	https://placehold.co/96x96/FEF3C7/D97706?text=TD	\N	2026-04-29 15:16:50.235386	\N
122	Demo company for TTJobs sample data in Customer Service.	Customer Service	Ho Chi Minh City	AnswerHub Support	https://answerhub.example.com	2026-04-29 15:16:50.235386	https://placehold.co/96x96/E0F2FE/0284C7?text=AH	\N	2026-04-29 15:16:50.235386	\N
123	Demo company for TTJobs sample data in Real Estate.	Real Estate	Da Nang	MetroSpace Property	https://metrospace.example.com	2026-04-29 15:16:50.235386	https://placehold.co/96x96/F3E8FF/9333EA?text=MS	\N	2026-04-29 15:16:50.235386	\N
124	Demo company for TTJobs sample data in Accounting.	Accounting	Ha Noi	ClearBooks Consulting	https://clearbooks.example.com	2026-04-29 15:16:50.235386	https://placehold.co/96x96/ECFCCB/65A30D?text=CB	\N	2026-04-29 15:16:50.235386	\N
125	Demo company for TTJobs sample data in Design.	Design	Remote	BrightUX Collective	https://brightux.example.com	2026-04-29 15:16:50.235386	https://placehold.co/96x96/FFE4E6/E11D48?text=BU	\N	2026-04-29 15:16:50.235386	\N
126	Demo company for TTJobs sample data in Business Development.	Business Development	Ho Chi Minh City	ScalePoint Ventures	https://scalepoint.example.com	2026-04-29 15:16:50.235386	https://placehold.co/96x96/CCFBF1/14B8A6?text=SP	\N	2026-04-29 15:16:50.235386	\N
127	Demo company for TTJobs sample data in Information Technology.	Information Technology	Da Nang	CodeHarbor Asia	https://codeharbor.example.com	2026-04-29 15:16:50.235386	https://placehold.co/96x96/E0E7FF/4F46E5?text=CH	\N	2026-04-29 15:16:50.235386	\N
128	Demo company for TTJobs sample data in Sales.	Sales	Ha Noi	PrimeSales Network	https://primesales.example.com	2026-04-29 15:16:50.235386	https://placehold.co/96x96/DBEAFE/1E40AF?text=PS	\N	2026-04-29 15:16:50.235386	\N
129	Demo company for TTJobs sample data in Marketing.	Marketing	Ho Chi Minh City	BrandPilot Agency	https://brandpilot.example.com	2026-04-29 15:16:50.235386	https://placehold.co/96x96/FCE7F3/BE185D?text=BA	\N	2026-04-29 15:16:50.235386	\N
130	Demo company for TTJobs sample data in Human Resources.	Human Resources	Da Nang	WorkWell People	https://workwell.example.com	2026-04-29 15:16:50.235386	https://placehold.co/96x96/DCFCE7/15803D?text=WW	\N	2026-04-29 15:16:50.235386	\N
131	Demo company for TTJobs sample data in Finance.	Finance	Remote	MoneyMap Analytics	https://moneymap.example.com	2026-04-29 15:16:50.235386	https://placehold.co/96x96/FEF3C7/B45309?text=MM	\N	2026-04-29 15:16:50.235386	\N
132	Demo company for TTJobs sample data in Customer Service.	Customer Service	Ha Noi	HappyDesk CX	https://happydesk.example.com	2026-04-29 15:16:50.235386	https://placehold.co/96x96/E0F2FE/0369A1?text=HD	\N	2026-04-29 15:16:50.235386	\N
133	Demo company for TTJobs sample data in Real Estate.	Real Estate	Ho Chi Minh City	HomeGrid Realty	https://homegrid.example.com	2026-04-29 15:16:50.235386	https://placehold.co/96x96/F3E8FF/7E22CE?text=HG	\N	2026-04-29 15:16:50.235386	\N
134	Demo company for TTJobs sample data in Accounting.	Accounting	Remote	TaxWise Partners	https://taxwise.example.com	2026-04-29 15:16:50.235386	https://placehold.co/96x96/ECFCCB/4D7C0F?text=TW	\N	2026-04-29 15:16:50.235386	\N
135	Demo company for TTJobs sample data in Design.	Design	Ha Noi	MotionBox Design	https://motionbox.example.com	2026-04-29 15:16:50.235386	https://placehold.co/96x96/FFE4E6/BE123C?text=MB	\N	2026-04-29 15:16:50.235386	\N
136	Demo company for TTJobs sample data in Business Development.	Business Development	Da Nang	DealCraft Global	https://dealcraft.example.com	2026-04-29 15:16:50.235386	https://placehold.co/96x96/CCFBF1/0F766E?text=DC	\N	2026-04-29 15:16:50.235386	\N
137	\N	\N	\N	company-30848391387500	\N	2026-04-29 15:17:36.717715	\N	317	2026-04-29 15:17:36.717715	\N
138	\N	\N	\N	company-30848613487900	\N	2026-04-29 15:17:36.937618	\N	320	2026-04-29 15:17:36.937618	\N
139	\N	\N	\N	company-30848909140600	\N	2026-04-29 15:17:37.233998	\N	321	2026-04-29 15:17:37.233998	\N
140	\N	\N	\N	company-31526241324200	\N	2026-04-29 15:28:54.566987	\N	325	2026-04-29 15:28:54.566987	\N
141	\N	\N	\N	company-31526465338800	\N	2026-04-29 15:28:54.790518	\N	328	2026-04-29 15:28:54.790518	\N
142	\N	\N	\N	company-31526859406100	\N	2026-04-29 15:28:55.184317	\N	329	2026-04-29 15:28:55.184317	\N
143	\N	\N	\N	company-41629476736100	\N	2026-04-29 18:17:17.802894	\N	333	2026-04-29 18:17:17.802894	\N
144	\N	\N	\N	company-41629624025200	\N	2026-04-29 18:17:17.949256	\N	336	2026-04-29 18:17:17.949256	\N
145	\N	\N	\N	company-41629787978600	\N	2026-04-29 18:17:18.112811	\N	337	2026-04-29 18:17:18.112811	\N
146	\N	\N	\N	company-41923226596800	\N	2026-04-29 18:22:11.552433	\N	341	2026-04-29 18:22:11.552433	\N
147	\N	\N	\N	company-41923484827200	\N	2026-04-29 18:22:11.809368	\N	344	2026-04-29 18:22:11.809368	\N
148	\N	\N	\N	company-41923698070100	\N	2026-04-29 18:22:12.022872	\N	345	2026-04-29 18:22:12.022872	\N
149	\N	\N	\N	company-120930657534800	\N	2026-05-01 16:45:48.814967	\N	349	2026-05-01 16:45:48.814967	\N
150	\N	\N	\N	company-120930815655700	\N	2026-05-01 16:45:48.972296	\N	352	2026-05-01 16:45:48.972296	\N
151	\N	\N	\N	company-120931004770500	\N	2026-05-01 16:45:49.147203	\N	353	2026-05-01 16:45:49.147203	\N
152	\N	\N	\N	company-123126361588400	\N	2026-05-01 17:22:24.518478	\N	357	2026-05-01 17:22:24.518478	\N
153	\N	\N	\N	company-123126497997000	\N	2026-05-01 17:22:24.655309	\N	360	2026-05-01 17:22:24.655309	\N
154	\N	\N	\N	company-123126658749300	\N	2026-05-01 17:22:24.815447	\N	361	2026-05-01 17:22:24.815447	\N
155	\N	\N	\N	company-130744229025600	\N	2026-05-01 19:29:22.386975	\N	365	2026-05-01 19:29:22.386975	\N
156	\N	\N	\N	company-130744543877600	\N	2026-05-01 19:29:22.687731	\N	368	2026-05-01 19:29:22.687731	\N
157	\N	\N	\N	company-130744934065300	\N	2026-05-01 19:29:23.090737	\N	369	2026-05-01 19:29:23.090737	\N
158	\N	\N	\N	company-94217478629000	\N	2026-05-06 07:30:34.804052	\N	373	2026-05-06 07:30:34.804052	\N
159	\N	\N	\N	company-94217571553300	\N	2026-05-06 07:30:34.895658	\N	376	2026-05-06 07:30:34.895658	\N
160	\N	\N	\N	company-94217663634300	\N	2026-05-06 07:30:34.987713	\N	377	2026-05-06 07:30:34.987713	\N
161	\N	\N	\N	company-94880398184200	\N	2026-05-06 07:41:37.556928	\N	381	2026-05-06 07:41:37.556928	\N
162	\N	\N	\N	company-94880842381400	\N	2026-05-06 07:41:38.000656	\N	384	2026-05-06 07:41:38.000656	\N
163	\N	\N	\N	company-94881237303100	\N	2026-05-06 07:41:38.395684	\N	385	2026-05-06 07:41:38.395684	\N
\.


--
-- Data for Name: company_follows; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.company_follows (id, user_id, company_id, followed_at) FROM stdin;
2	177	122	2026-05-06 08:00:13.916582
\.


--
-- Data for Name: company_members; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.company_members (id, company_id, user_id, member_role, created_at) FROM stdin;
1	3	9	ADMIN	2026-04-03 09:57:38.131213
2	4	14	ADMIN	2026-04-03 09:57:38.131213
3	6	44	ADMIN	2026-04-03 10:24:05.149102
4	9	52	ADMIN	2026-04-03 10:25:03.071316
5	12	60	ADMIN	2026-04-03 10:25:41.721427
6	15	68	ADMIN	2026-04-03 20:35:21.330521
7	18	76	ADMIN	2026-04-03 20:35:53.735077
8	21	84	ADMIN	2026-04-03 21:22:32.617985
9	24	92	ADMIN	2026-04-03 21:22:57.596671
10	27	100	ADMIN	2026-04-03 21:47:34.548938
11	30	108	ADMIN	2026-04-03 21:48:17.448878
12	33	116	ADMIN	2026-04-03 22:20:46.952805
13	36	124	ADMIN	2026-04-03 23:49:05.801888
14	39	132	ADMIN	2026-04-03 23:49:42.56308
15	42	140	ADMIN	2026-04-04 19:42:59.363676
16	45	148	ADMIN	2026-04-05 11:26:31.116163
17	48	156	ADMIN	2026-04-05 12:19:13.16884
18	51	165	ADMIN	2026-04-13 14:17:32.325738
19	54	173	ADMIN	2026-04-13 14:19:47.991704
20	57	183	ADMIN	2026-04-24 12:19:57.893684
21	60	191	ADMIN	2026-04-24 12:21:26.7337
22	63	199	ADMIN	2026-04-24 12:22:26.589201
23	66	207	ADMIN	2026-04-24 12:30:29.187569
24	69	215	ADMIN	2026-04-24 15:58:14.36246
25	72	223	ADMIN	2026-04-24 20:12:48.011584
26	75	231	ADMIN	2026-04-24 20:16:50.769492
27	78	240	ADMIN	2026-04-27 18:19:25.036086
28	81	248	ADMIN	2026-04-27 18:21:41.501818
29	84	256	ADMIN	2026-04-29 08:18:06.619816
30	87	264	ADMIN	2026-04-29 08:20:39.039479
31	90	272	ADMIN	2026-04-29 08:25:08.672551
32	93	280	ADMIN	2026-04-29 09:22:12.861917
33	96	288	ADMIN	2026-04-29 10:08:19.390276
34	99	296	ADMIN	2026-04-29 14:57:08.39379
35	102	304	ADMIN	2026-04-29 14:59:28.223078
36	105	312	ADMIN	2026-04-29 15:01:17.365052
37	138	320	ADMIN	2026-04-29 15:17:36.942839
38	141	328	ADMIN	2026-04-29 15:28:54.793601
39	144	336	ADMIN	2026-04-29 18:17:17.953246
40	147	344	ADMIN	2026-04-29 18:22:11.813924
41	150	352	ADMIN	2026-05-01 16:45:48.975296
42	153	360	ADMIN	2026-05-01 17:22:24.658425
43	156	368	ADMIN	2026-05-01 19:29:22.705425
44	159	376	ADMIN	2026-05-06 07:30:34.898221
45	162	384	ADMIN	2026-05-06 07:41:38.006704
\.


--
-- Data for Name: company_reviews; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.company_reviews (id, company_id, user_id, rating, pros, cons, salary, anonymous, created_at, updated_at) FROM stdin;
\.


--
-- Data for Name: conversation_members; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.conversation_members (conversation_id, user_id, created_at, last_read_at) FROM stdin;
1	1	2026-04-25 15:08:00.29714	2026-04-27 19:57:54.535953
1	6	2026-04-25 15:08:00.29714	2026-04-27 19:57:54.535953
3	6	2026-04-27 21:04:59.887757	2026-04-27 21:25:45.420614
3	159	2026-04-27 21:04:59.887757	2026-04-27 21:25:45.420614
2	6	2026-04-27 20:20:29.64664	2026-04-28 07:39:41.022238
2	234	2026-04-27 20:20:29.64664	2026-04-28 07:39:41.022238
\.


--
-- Data for Name: conversations; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.conversations (id, created_at) FROM stdin;
1	2026-04-25 15:08:00.289533
2	2026-04-27 20:20:29.641974
3	2026-04-27 21:04:59.885745
\.


--
-- Data for Name: cvs; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.cvs (id, file_url, uploaded_at, user_id) FROM stdin;
\.


--
-- Data for Name: email_change_verifications; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.email_change_verifications (id, user_id, new_email, code, expires_at, used_at, created_at) FROM stdin;
1	177	admin1@gmail.com	411925	2026-05-04 14:14:54.113534	\N	2026-05-04 14:04:54.116588
2	177	phimjvay@gmail.com	226977	2026-05-04 14:15:09.388929	\N	2026-05-04 14:05:09.389938
3	177	nguyenthanhthinh020@gmail.com	824764	2026-05-04 14:16:35.198825	\N	2026-05-04 14:06:35.198825
4	177	thinhnt.23it@vku.udn.vn	207091	2026-05-04 14:26:15.496741	\N	2026-05-04 14:16:15.496741
5	177	thinhnt.23it@vku.udn.vn	919396	2026-05-04 14:30:11.783652	\N	2026-05-04 14:20:11.795498
6	177	thinhnt.23it@vku.udn.vn	569245	2026-05-04 14:30:31.553216	2026-05-04 14:21:22.274815	2026-05-04 14:20:31.554744
\.


--
-- Data for Name: flyway_schema_history; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.flyway_schema_history (installed_rank, version, description, type, script, checksum, installed_by, installed_on, execution_time, success) FROM stdin;
1	1	<< Flyway Baseline >>	BASELINE	<< Flyway Baseline >>	\N	postgres	2026-04-02 22:10:08.224716	0	t
2	2	users auth hardening	SQL	V2__users_auth_hardening.sql	-1775169179	postgres	2026-04-02 22:10:08.381551	59	t
3	3	jobs and applications status backfill	SQL	V3__jobs_and_applications_status_backfill.sql	-1420716352	postgres	2026-04-02 22:10:08.51775	13	t
4	4	job application status audit	SQL	V4__job_application_status_audit.sql	1564987123	postgres	2026-04-03 08:21:15.042788	93	t
5	5	phase4 schema hardening and company members	SQL	V5__phase4_schema_hardening_and_company_members.sql	770434102	postgres	2026-04-03 09:57:37.915709	401	t
6	6	notifications in app	SQL	V6__notifications_in_app.sql	-1725702783	postgres	2026-04-03 20:35:14.329163	167	t
7	7	saved jobs	SQL	V7__saved_jobs.sql	1158977299	postgres	2026-04-03 21:22:27.959396	97	t
8	8	saved jobs trigger	SQL	V8__saved_jobs_trigger.sql	-330350981	postgres	2026-04-03 21:33:35.117666	94	t
9	9	chat conversations messages	SQL	V9__chat_conversations_messages.sql	-1120025176	postgres	2026-04-06 08:15:40.80849	190	t
10	10	alerts preferences and saved job notes	SQL	V10__alerts_preferences_and_saved_job_notes.sql	-1593369847	postgres	2026-04-06 08:15:41.045576	26	t
11	11	drop job alerts	SQL	V11__drop_job_alerts.sql	-1806036477	postgres	2026-04-06 08:15:41.085603	16	t
12	12	add user cv text	SQL	V12__add_user_cv_text.sql	-2089838817	postgres	2026-04-06 08:15:41.123962	3	t
13	13	add job category	SQL	V13__add_job_category.sql	-1071171995	postgres	2026-04-06 08:15:41.138418	6	t
14	14	user cvs and application cv	SQL	V14__user_cvs_and_application_cv.sql	-838055632	postgres	2026-04-08 20:03:16.430501	104	t
15	15	company follows	SQL	V15__company_follows.sql	231300681	postgres	2026-04-18 22:47:53.804807	133	t
16	16	add cv builder fields	SQL	V16__add_cv_builder_fields.sql	-1019439313	postgres	2026-04-20 11:24:56.537182	44	t
17	17	add user primary cv type	SQL	V17__add_user_primary_cv_type.sql	-713588213	postgres	2026-04-24 12:19:31.697751	38	t
18	18	career guide articles	SQL	V18__career_guide_articles.sql	287798316	postgres	2026-04-24 12:27:23.936229	75	t
19	19	job need preferences	SQL	V19__job_need_preferences.sql	554061639	postgres	2026-04-24 15:50:38.065458	37	t
20	20	recruiter activity logs	SQL	V20__recruiter_activity_logs.sql	-1400642822	postgres	2026-04-25 12:50:34.70932	104	t
21	21	message read receipts and attachments	SQL	V21__message_read_receipts_and_attachments.sql	-1326101601	postgres	2026-04-25 22:18:57.725461	53	t
22	22	recruiter growth features	SQL	V22__recruiter_growth_features.sql	-1209372639	postgres	2026-04-26 19:21:32.680241	143	t
23	23	notification target url	SQL	V23__notification_target_url.sql	-593291179	postgres	2026-04-27 20:11:35.663392	28	t
24	24	job image url	SQL	V24__job_image_url.sql	-1680988500	postgres	2026-04-27 21:09:29.086614	2	t
25	25	job need matching fields	SQL	V25__job_need_matching_fields.sql	-2005208916	postgres	2026-04-29 14:44:52.366774	27	t
26	26	demo companies and jobs	SQL	V26__demo_companies_and_jobs.sql	2076101473	postgres	2026-04-29 15:16:50.176338	63	t
27	27	candidate job matches	SQL	V27__candidate_job_matches.sql	-1331784509	postgres	2026-05-02 19:43:38.354656	111	t
28	28	application ai scores	SQL	V28__application_ai_scores.sql	1425497347	postgres	2026-05-03 14:48:20.269231	121	t
29	29	user tool sessions	SQL	V29__user_tool_sessions.sql	-1314092110	postgres	2026-05-03 21:48:19.761002	81	t
30	30	user personality profile	SQL	V30__user_personality_profile.sql	1542643261	postgres	2026-05-03 21:48:19.851773	5	t
31	31	job application cover letter	SQL	V31__job_application_cover_letter.sql	-893803518	postgres	2026-05-03 21:48:19.862681	2	t
32	32	job application cv text snapshot	SQL	V32__job_application_cv_text_snapshot.sql	1402791239	postgres	2026-05-04 08:34:47.079938	17	t
33	33	job alert history	SQL	V33__job_alert_history.sql	2126565813	postgres	2026-05-04 10:38:15.711765	122	t
34	34	email change verifications	SQL	V34__email_change_verifications.sql	1920710712	postgres	2026-05-04 10:46:41.399476	29	t
35	35	candidate experience recruiter tools	SQL	V35__candidate_experience_recruiter_tools.sql	388485125	postgres	2026-05-06 07:30:19.602656	106	t
\.


--
-- Data for Name: interview_schedules; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.interview_schedules (id, application_id, recruiter_id, candidate_id, scheduled_at, duration_minutes, location, meeting_link, note, status, created_at, updated_at) FROM stdin;
1	7	6	159	2026-05-07 14:32:00	30	Đà Nẵng			pending	2026-05-01 21:39:08.518308	2026-05-01 21:39:08.518308
2	8	6	234	2026-05-01 14:39:00	30				confirmed	2026-05-01 21:39:33.333585	2026-05-01 22:18:00.341876
\.


--
-- Data for Name: job_alert_history; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.job_alert_history (id, user_id, job_id, sent_at) FROM stdin;
\.


--
-- Data for Name: job_application_status_audits; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.job_application_status_audits (id, application_id, changed_by_id, from_status, to_status, changed_at) FROM stdin;
1	2	6	submitted	reviewing	2026-04-25 12:56:19.659916
2	3	1	\N	submitted	2026-04-27 20:01:16.242833
3	3	6	submitted	reviewing	2026-04-27 20:17:43.80434
4	3	6	reviewing	interviewed	2026-04-27 20:17:46.192133
5	3	6	interviewed	offered	2026-04-27 20:17:47.720077
6	3	6	offered	hired	2026-04-27 20:17:50.153894
7	4	234	\N	submitted	2026-04-27 20:20:12.287881
8	5	234	\N	submitted	2026-04-27 20:20:48.377784
9	4	234	submitted	withdrawn	2026-04-27 20:36:48.99978
10	5	234	submitted	withdrawn	2026-04-27 20:36:52.766851
11	6	234	\N	submitted	2026-04-27 20:37:34.300611
12	7	159	\N	submitted	2026-04-27 20:38:02.963742
13	8	234	\N	submitted	2026-05-01 21:26:05.832199
14	7	6	submitted	reviewing	2026-05-01 21:28:47.685015
15	7	6	reviewing	shortlisted	2026-05-01 21:28:49.84263
16	2	6	reviewing	shortlisted	2026-05-03 19:22:07.281014
17	9	177	\N	submitted	2026-05-04 07:31:42.875087
18	10	177	\N	submitted	2026-05-04 09:11:54.672223
\.


--
-- Data for Name: job_applications; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.job_applications (id, application_date, status, job_id, user_id, updated_at, cv_url, cv_file_name, cv_id, cover_letter, cv_text_snapshot) FROM stdin;
1	2026-03-31 15:37:05.777862	submitted	2	1	2026-03-31 15:37:05.777862	\N	\N	\N	\N	\N
3	2026-04-27 20:01:11.31899	hired	4	1	2026-04-27 20:17:50.150921	https://res.cloudinary.com/dgny2gq8p/raw/upload/v1777294876/ttjobs/cv/applications/app-1-1777294871649	Midterm_Review_OS_ans.pdf	1	\N	\N
4	2026-04-27 20:20:08.059641	withdrawn	4	234	2026-04-27 20:36:48.997573	https://res.cloudinary.com/dgny2gq8p/raw/upload/v1777296012/ttjobs/cv/applications/app-234-1777296008065	SP(3)2025_Midterm- Ownership and Permissions+ System and User Security .pdf	2	\N	\N
5	2026-04-27 20:20:48.373229	withdrawn	3	234	2026-04-27 20:36:52.764579	https://res.cloudinary.com/dgny2gq8p/raw/upload/v1777296012/ttjobs/cv/applications/app-234-1777296008065	SP(3)2025_Midterm- Ownership and Permissions+ System and User Security .pdf	2	\N	\N
6	2026-04-27 20:37:34.296593	submitted	2	234	2026-04-27 20:37:34.297629	https://res.cloudinary.com/dgny2gq8p/raw/upload/v1777296012/ttjobs/cv/applications/app-234-1777296008065	SP(3)2025_Midterm- Ownership and Permissions+ System and User Security .pdf	2	\N	\N
8	2026-05-01 21:26:01.222864	submitted	306	234	2026-05-01 21:26:05.827167	https://res.cloudinary.com/dgny2gq8p/raw/upload/v1777645564/ttjobs/cv/applications/app-234-1777645561488	Midterm_Review_OS_ans.pdf	4	\N	\N
7	2026-04-27 20:37:59.854454	shortlisted	2	159	2026-05-01 21:28:49.839661	https://res.cloudinary.com/dgny2gq8p/raw/upload/v1777297082/ttjobs/cv/applications/app-159-1777297079855	TTCĐ2.docx	3	\N	\N
2	2026-03-31 15:44:10.729477	shortlisted	3	1	2026-05-03 19:22:07.266607	\N	\N	\N	\N	\N
9	2026-05-04 07:31:39.906208	submitted	306	177	2026-05-04 07:31:42.869462	https://res.cloudinary.com/dgny2gq8p/raw/upload/v1777854703/ttjobs/cv/applications/app-177-1777854699907	10089434.pdf	5	\N	\N
10	2026-05-04 09:11:54.658655	submitted	53	177	2026-05-04 09:11:54.672223	\N	ttjobs-system-cv-177.txt	\N	\N	Ho ten: Hoang Dat\nEmail: nguyenthanhthinh020812@gmail.com\nVi tri ung tuyen: IT\nMuc tieu nghe nghiep: JAVA\nKinh nghiem noi bat: java, Python\nKy nang: Docker, Python, Java, Github
\.


--
-- Data for Name: job_need_preferences; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.job_need_preferences (user_id, desired_title, desired_location, desired_category, desired_job_type, desired_experience_level, min_salary, max_salary, remote_only, created_at, updated_at, preferred_skills, excluded_keywords) FROM stdin;
1	\N	\N	\N	\N	\N	\N	\N	f	2026-04-27 18:41:17.719745	2026-04-27 18:41:17.719745	\N	\N
234	\N	\N	\N	\N	\N	\N	\N	f	2026-04-27 20:37:19.476198	2026-04-27 20:37:19.476198	\N	\N
6	Backend, Dev ops	Đà Nẵng				5000000.00	300000000.00	f	2026-04-29 10:10:31.913136	2026-04-29 19:56:43.681517	Java	intern
177	Backend, Dev ops	Đà Nẵng				1000000.00	30000000.00	f	2026-04-24 18:30:16.965565	2026-05-02 19:29:30.315546	JAVA	intern
\.


--
-- Data for Name: job_skills; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.job_skills (job_id, skill_id) FROM stdin;
16	26
16	7
16	24
16	15
17	6
17	10
17	5
17	16
18	26
18	7
18	24
18	15
19	6
19	10
19	5
19	16
20	26
20	7
20	24
20	15
21	6
21	10
21	5
21	16
22	26
22	7
22	24
22	15
23	6
23	10
23	5
23	16
5	34
5	29
5	11
5	4
5	12
6	33
6	3
6	5
6	10
7	34
7	29
7	11
7	4
7	12
8	33
8	3
8	5
8	10
9	34
9	29
9	11
9	4
9	12
10	33
10	3
10	5
10	10
11	34
11	29
11	11
11	4
11	12
12	33
12	3
12	5
12	10
13	34
13	29
13	11
13	4
13	12
14	33
14	3
14	5
14	10
15	6
15	10
15	5
15	16
24	26
24	7
24	24
24	15
25	25
25	22
25	8
25	30
26	20
26	13
26	18
26	23
27	25
27	22
27	8
27	30
28	20
28	13
28	18
28	23
29	25
29	22
29	8
29	30
30	20
30	13
30	18
30	23
31	25
31	22
31	8
31	30
32	20
32	13
32	18
32	23
33	25
33	22
33	8
33	30
34	20
34	13
34	18
34	23
35	31
35	14
35	28
35	23
36	27
36	1
36	2
36	5
37	31
37	14
37	28
37	23
38	27
38	1
38	2
38	5
39	31
39	14
39	28
39	23
40	27
40	1
40	2
40	5
41	31
41	14
41	28
41	23
42	27
42	1
42	2
42	5
43	31
43	14
43	28
43	23
44	27
44	1
44	2
44	5
45	17
45	9
45	15
45	4
46	19
46	32
46	12
46	4
47	17
47	9
47	15
47	4
48	19
48	32
48	12
48	4
49	17
49	9
49	15
49	4
50	19
50	32
50	12
50	4
51	17
51	9
51	15
51	4
52	19
52	32
52	12
52	4
53	17
53	9
53	15
53	4
54	19
54	32
54	12
54	4
55	21
55	23
55	10
55	16
56	15
56	23
56	16
56	2
57	21
57	23
57	10
57	16
58	15
58	23
58	16
58	2
59	21
59	23
59	10
59	16
60	15
60	23
60	16
60	2
61	21
61	23
61	10
61	16
62	15
62	23
62	16
62	2
63	21
63	23
63	10
63	16
64	15
64	23
64	16
64	2
65	33
65	3
65	5
65	10
66	34
66	29
66	11
66	4
66	12
67	33
67	3
67	5
67	10
68	34
68	29
68	11
68	4
68	12
69	33
69	3
69	5
69	10
70	34
70	29
70	11
70	4
70	12
71	33
71	3
71	5
71	10
72	34
72	29
72	11
72	4
72	12
73	33
73	3
73	5
73	10
74	34
74	29
74	11
74	4
74	12
75	26
75	7
75	24
75	15
76	6
76	10
76	5
76	16
77	26
77	7
77	24
77	15
78	6
78	10
78	5
78	16
79	26
79	7
79	24
79	15
80	6
80	10
80	5
80	16
81	26
81	7
81	24
81	15
82	6
82	10
82	5
82	16
83	26
83	7
83	24
83	15
84	6
84	10
84	5
84	16
85	20
85	13
85	18
85	23
86	25
86	22
86	8
86	30
87	20
87	13
87	18
87	23
88	25
88	22
88	8
88	30
89	20
89	13
89	18
89	23
90	25
90	22
90	8
90	30
91	20
91	13
91	18
91	23
92	25
92	22
92	8
92	30
93	20
93	13
93	18
93	23
94	25
94	22
94	8
94	30
95	27
95	1
95	2
95	5
96	31
96	14
96	28
96	23
97	27
97	1
97	2
97	5
98	31
98	14
98	28
98	23
99	27
99	1
99	2
99	5
100	31
100	14
100	28
100	23
101	27
101	1
101	2
101	5
102	31
102	14
102	28
102	23
103	27
103	1
103	2
103	5
104	31
104	14
104	28
104	23
105	19
105	32
105	12
105	4
106	17
106	9
106	15
106	4
107	19
107	32
107	12
107	4
108	17
108	9
108	15
108	4
109	19
109	32
109	12
109	4
110	17
110	9
110	15
110	4
111	19
111	32
111	12
111	4
112	17
112	9
112	15
112	4
113	19
113	32
113	12
113	4
114	17
114	9
114	15
114	4
115	15
115	23
115	16
115	2
116	21
116	23
116	10
116	16
117	15
117	23
117	16
117	2
118	21
118	23
118	10
118	16
119	15
119	23
119	16
119	2
120	21
120	23
120	10
120	16
121	15
121	23
121	16
121	2
122	21
122	23
122	10
122	16
123	15
123	23
123	16
123	2
124	21
124	23
124	10
124	16
125	34
125	29
125	11
125	4
125	12
126	33
126	3
126	5
126	10
127	34
127	29
127	11
127	4
127	12
128	33
128	3
128	5
128	10
129	34
129	29
129	11
129	4
129	12
130	33
130	3
130	5
130	10
131	34
131	29
131	11
131	4
131	12
132	33
132	3
132	5
132	10
133	34
133	29
133	11
133	4
133	12
134	33
134	3
134	5
134	10
135	6
135	10
135	5
135	16
136	26
136	7
136	24
136	15
137	6
137	10
137	5
137	16
138	26
138	7
138	24
138	15
139	6
139	10
139	5
139	16
140	26
140	7
140	24
140	15
141	6
141	10
141	5
141	16
142	26
142	7
142	24
142	15
143	6
143	10
143	5
143	16
144	26
144	7
144	24
144	15
145	25
145	22
145	8
145	30
146	20
146	13
146	18
146	23
147	25
147	22
147	8
147	30
148	20
148	13
148	18
148	23
149	25
149	22
149	8
149	30
150	20
150	13
150	18
150	23
151	25
151	22
151	8
151	30
152	20
152	13
152	18
152	23
153	25
153	22
153	8
153	30
154	20
154	13
154	18
154	23
155	31
155	14
155	28
155	23
156	27
156	1
156	2
156	5
157	31
157	14
157	28
157	23
158	27
158	1
158	2
158	5
159	31
159	14
159	28
159	23
160	27
160	1
160	2
160	5
161	31
161	14
161	28
161	23
162	27
162	1
162	2
162	5
163	31
163	14
163	28
163	23
164	27
164	1
164	2
164	5
165	17
165	9
165	15
165	4
166	19
166	32
166	12
166	4
167	17
167	9
167	15
167	4
168	19
168	32
168	12
168	4
169	17
169	9
169	15
169	4
170	19
170	32
170	12
170	4
171	17
171	9
171	15
171	4
172	19
172	32
172	12
172	4
173	17
173	9
173	15
173	4
174	19
174	32
174	12
174	4
175	21
175	23
175	10
175	16
176	15
176	23
176	16
176	2
177	21
177	23
177	10
177	16
178	15
178	23
178	16
178	2
179	21
179	23
179	10
179	16
180	15
180	23
180	16
180	2
181	21
181	23
181	10
181	16
182	15
182	23
182	16
182	2
183	21
183	23
183	10
183	16
184	15
184	23
184	16
184	2
185	33
185	3
185	5
185	10
186	34
186	29
186	11
186	4
186	12
187	33
187	3
187	5
187	10
188	34
188	29
188	11
188	4
188	12
189	33
189	3
189	5
189	10
190	34
190	29
190	11
190	4
190	12
191	33
191	3
191	5
191	10
192	34
192	29
192	11
192	4
192	12
193	33
193	3
193	5
193	10
194	34
194	29
194	11
194	4
194	12
195	26
195	7
195	24
195	15
196	6
196	10
196	5
196	16
197	26
197	7
197	24
197	15
198	6
198	10
198	5
198	16
199	26
199	7
199	24
199	15
200	6
200	10
200	5
200	16
201	26
201	7
201	24
201	15
202	6
202	10
202	5
202	16
203	26
203	7
203	24
203	15
204	6
204	10
204	5
204	16
205	20
205	13
205	18
205	23
206	25
206	22
206	8
206	30
207	20
207	13
207	18
207	23
208	25
208	22
208	8
208	30
209	20
209	13
209	18
209	23
210	25
210	22
210	8
210	30
211	20
211	13
211	18
211	23
212	25
212	22
212	8
212	30
213	20
213	13
213	18
213	23
214	25
214	22
214	8
214	30
215	27
215	1
215	2
215	5
216	31
216	14
216	28
216	23
217	27
217	1
217	2
217	5
218	31
218	14
218	28
218	23
219	27
219	1
219	2
219	5
220	31
220	14
220	28
220	23
221	27
221	1
221	2
221	5
222	31
222	14
222	28
222	23
223	27
223	1
223	2
223	5
224	31
224	14
224	28
224	23
225	19
225	32
225	12
225	4
226	17
226	9
226	15
226	4
227	19
227	32
227	12
227	4
228	17
228	9
228	15
228	4
229	19
229	32
229	12
229	4
230	17
230	9
230	15
230	4
231	19
231	32
231	12
231	4
232	17
232	9
232	15
232	4
233	19
233	32
233	12
233	4
234	17
234	9
234	15
234	4
235	15
235	23
235	16
235	2
236	21
236	23
236	10
236	16
237	15
237	23
237	16
237	2
238	21
238	23
238	10
238	16
239	15
239	23
239	16
239	2
240	21
240	23
240	10
240	16
241	15
241	23
241	16
241	2
242	21
242	23
242	10
242	16
243	15
243	23
243	16
243	2
244	21
244	23
244	10
244	16
245	34
245	29
245	11
245	4
245	12
246	33
246	3
246	5
246	10
247	34
247	29
247	11
247	4
247	12
248	33
248	3
248	5
248	10
249	34
249	29
249	11
249	4
249	12
250	33
250	3
250	5
250	10
251	34
251	29
251	11
251	4
251	12
252	33
252	3
252	5
252	10
253	34
253	29
253	11
253	4
253	12
254	33
254	3
254	5
254	10
255	6
255	10
255	5
255	16
256	26
256	7
256	24
256	15
257	6
257	10
257	5
257	16
258	26
258	7
258	24
258	15
259	6
259	10
259	5
259	16
260	26
260	7
260	24
260	15
261	6
261	10
261	5
261	16
262	26
262	7
262	24
262	15
263	6
263	10
263	5
263	16
264	26
264	7
264	24
264	15
265	25
265	22
265	8
265	30
266	20
266	13
266	18
266	23
267	25
267	22
267	8
267	30
268	20
268	13
268	18
268	23
269	25
269	22
269	8
269	30
270	20
270	13
270	18
270	23
271	25
271	22
271	8
271	30
272	20
272	13
272	18
272	23
273	25
273	22
273	8
273	30
274	20
274	13
274	18
274	23
275	31
275	14
275	28
275	23
276	27
276	1
276	2
276	5
277	31
277	14
277	28
277	23
278	27
278	1
278	2
278	5
279	31
279	14
279	28
279	23
280	27
280	1
280	2
280	5
281	31
281	14
281	28
281	23
282	27
282	1
282	2
282	5
283	31
283	14
283	28
283	23
284	27
284	1
284	2
284	5
285	17
285	9
285	15
285	4
286	19
286	32
286	12
286	4
287	17
287	9
287	15
287	4
288	19
288	32
288	12
288	4
289	17
289	9
289	15
289	4
290	19
290	32
290	12
290	4
291	17
291	9
291	15
291	4
292	19
292	32
292	12
292	4
293	17
293	9
293	15
293	4
294	19
294	32
294	12
294	4
295	21
295	23
295	10
295	16
296	15
296	23
296	16
296	2
297	21
297	23
297	10
297	16
298	15
298	23
298	16
298	2
299	21
299	23
299	10
299	16
300	15
300	23
300	16
300	2
301	21
301	23
301	10
301	16
302	15
302	23
302	16
302	2
303	21
303	23
303	10
303	16
304	15
304	23
304	16
304	2
\.


--
-- Data for Name: jobs; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.jobs (id, application_deadline, description, experience_level, job_type, location, posted_date, salary, title, company_id, status, salary_min, salary_max, currency, updated_at, deleted_at, category, image_url) FROM stdin;
16	2026-06-19 15:16:50.235386	Demo seed: Handle accounting records, reports and compliance tasks. Key skills: Accounting,Tax,Audit,Excel. Job type: Part-time.	Junior	Part-time	Ho Chi Minh City	2026-03-29 15:16:50.235386	17500000.00	Accountant Junior #032	108	open	13500000	21500000	VND	2026-04-29 15:16:50.235386	\N	ACCOUNTING	https://placehold.co/640x360/EFF6FF/2563EB?text=TTJobs
17	2026-06-04 15:16:50.235386	Demo seed: Own sales pipeline and close new business opportunities. Key skills: Sales,CRM,Negotiation,Customer Success. Job type: Part-time.	Junior	Part-time	Ho Chi Minh City	2026-04-13 15:16:50.235386	20500000.00	Sales Executive Junior #062	108	open	16500000	24500000	VND	2026-04-29 15:16:50.235386	\N	SALES	https://placehold.co/640x360/EFF6FF/2563EB?text=TTJobs
18	2026-05-20 15:16:50.235386	Demo seed: Handle accounting records, reports and compliance tasks. Key skills: Accounting,Tax,Audit,Excel. Job type: Part-time.	Junior	Part-time	Ho Chi Minh City	2026-04-28 15:16:50.235386	13000000.00	Accountant Junior #092	108	open	9000000	17000000	VND	2026-04-29 15:16:50.235386	\N	ACCOUNTING	https://placehold.co/640x360/EFF6FF/2563EB?text=TTJobs
19	2026-06-19 15:16:50.235386	Demo seed: Own sales pipeline and close new business opportunities. Key skills: Sales,CRM,Negotiation,Customer Success. Job type: Part-time.	Junior	Part-time	Ho Chi Minh City	2026-03-29 15:16:50.235386	16000000.00	Sales Executive Junior #122	108	open	12000000	20000000	VND	2026-04-29 15:16:50.235386	\N	SALES	https://placehold.co/640x360/EFF6FF/2563EB?text=TTJobs
20	2026-06-04 15:16:50.235386	Demo seed: Handle accounting records, reports and compliance tasks. Key skills: Accounting,Tax,Audit,Excel. Job type: Part-time.	Junior	Part-time	Ho Chi Minh City	2026-04-13 15:16:50.235386	19000000.00	Accountant Junior #152	108	open	15000000	23000000	VND	2026-04-29 15:16:50.235386	\N	ACCOUNTING	https://placehold.co/640x360/EFF6FF/2563EB?text=TTJobs
21	2026-05-20 15:16:50.235386	Demo seed: Own sales pipeline and close new business opportunities. Key skills: Sales,CRM,Negotiation,Customer Success. Job type: Part-time.	Junior	Part-time	Ho Chi Minh City	2026-04-28 15:16:50.235386	22000000.00	Sales Executive Junior #182	108	open	18000000	26000000	VND	2026-04-29 15:16:50.235386	\N	SALES	https://placehold.co/640x360/EFF6FF/2563EB?text=TTJobs
22	2026-06-19 15:16:50.235386	Demo seed: Handle accounting records, reports and compliance tasks. Key skills: Accounting,Tax,Audit,Excel. Job type: Part-time.	Junior	Part-time	Ho Chi Minh City	2026-03-29 15:16:50.235386	14500000.00	Accountant Junior #212	108	open	10500000	18500000	VND	2026-04-29 15:16:50.235386	\N	ACCOUNTING	https://placehold.co/640x360/EFF6FF/2563EB?text=TTJobs
23	2026-06-04 15:16:50.235386	Demo seed: Own sales pipeline and close new business opportunities. Key skills: Sales,CRM,Negotiation,Customer Success. Job type: Part-time.	Junior	Part-time	Ho Chi Minh City	2026-04-13 15:16:50.235386	17500000.00	Sales Executive Junior #242	108	open	13500000	21500000	VND	2026-04-29 15:16:50.235386	\N	SALES	https://placehold.co/640x360/EFF6FF/2563EB?text=TTJobs
3	2026-04-30 23:59:59	Develop Spring Boot APIs	MID	FULL_TIME	Đà Nẵng	2026-03-31 15:35:14.290144	1500.00	Java Backend Developer	2	open	1500.00	1500.00	VND	2026-03-31 15:35:14.290144	\N	\N	\N
2	2026-04-30 23:59:00	Phát triển API và tích hợp hệ thống	MID	FULL_TIME	Hà Nội	2026-03-30 20:23:04.686208	20000000.00	Java Backend Developer	1	open	20000000.00	20000000.00	VND	2026-03-30 20:23:04.686208	\N	INFORMATION-TECHNOLOGY	\N
4	2026-04-30 15:36:00	Job mới	ENTRY	Full-time	Đà Nẵng	2026-04-25 12:37:02.176887	\N	Tuyển dụng cho vị trí trưởng phòng	35	open	4000000	8000000	VND	2026-04-27 20:00:55.297692	\N	INFORMATION-TECHNOLOGY	\N
5	2026-05-19 15:16:50.235386	Demo seed: Build and maintain production software for growing product teams. Key skills: Java,Spring Boot,React,SQL,Docker. Job type: Full-time.	Entry	Full-time	Ha Noi	2026-04-29 15:16:50.235386	11000000.00	Software Engineer Entry #001	107	open	7000000	15000000	VND	2026-04-29 15:16:50.235386	\N	INFORMATION-TECHNOLOGY	https://placehold.co/640x360/EFF6FF/2563EB?text=TTJobs
6	2026-06-18 15:16:50.235386	Demo seed: Advise customers and manage real estate opportunities. Key skills: Real Estate Sales,Property Management,Negotiation,CRM. Job type: Full-time.	Entry	Full-time	Ha Noi	2026-03-30 15:16:50.235386	14000000.00	Property Consultant Entry #031	107	open	10000000	18000000	VND	2026-04-29 15:16:50.235386	\N	REAL-ESTATE	https://placehold.co/640x360/EFF6FF/2563EB?text=TTJobs
7	2026-06-03 15:16:50.235386	Demo seed: Build and maintain production software for growing product teams. Key skills: Java,Spring Boot,React,SQL,Docker. Job type: Full-time.	Entry	Full-time	Ha Noi	2026-04-14 15:16:50.235386	17000000.00	Software Engineer Entry #061	107	open	13000000	21000000	VND	2026-04-29 15:16:50.235386	\N	INFORMATION-TECHNOLOGY	https://placehold.co/640x360/EFF6FF/2563EB?text=TTJobs
8	2026-05-19 15:16:50.235386	Demo seed: Advise customers and manage real estate opportunities. Key skills: Real Estate Sales,Property Management,Negotiation,CRM. Job type: Full-time.	Entry	Full-time	Ha Noi	2026-04-29 15:16:50.235386	20000000.00	Property Consultant Entry #091	107	open	16000000	24000000	VND	2026-04-29 15:16:50.235386	\N	REAL-ESTATE	https://placehold.co/640x360/EFF6FF/2563EB?text=TTJobs
9	2026-06-18 15:16:50.235386	Demo seed: Build and maintain production software for growing product teams. Key skills: Java,Spring Boot,React,SQL,Docker. Job type: Full-time.	Entry	Full-time	Ha Noi	2026-03-30 15:16:50.235386	12500000.00	Software Engineer Entry #121	107	open	8500000	16500000	VND	2026-04-29 15:16:50.235386	\N	INFORMATION-TECHNOLOGY	https://placehold.co/640x360/EFF6FF/2563EB?text=TTJobs
10	2026-06-03 15:16:50.235386	Demo seed: Advise customers and manage real estate opportunities. Key skills: Real Estate Sales,Property Management,Negotiation,CRM. Job type: Full-time.	Entry	Full-time	Ha Noi	2026-04-14 15:16:50.235386	15500000.00	Property Consultant Entry #151	107	open	11500000	19500000	VND	2026-04-29 15:16:50.235386	\N	REAL-ESTATE	https://placehold.co/640x360/EFF6FF/2563EB?text=TTJobs
11	2026-05-19 15:16:50.235386	Demo seed: Build and maintain production software for growing product teams. Key skills: Java,Spring Boot,React,SQL,Docker. Job type: Full-time.	Entry	Full-time	Ha Noi	2026-04-29 15:16:50.235386	18500000.00	Software Engineer Entry #181	107	open	14500000	22500000	VND	2026-04-29 15:16:50.235386	\N	INFORMATION-TECHNOLOGY	https://placehold.co/640x360/EFF6FF/2563EB?text=TTJobs
12	2026-06-18 15:16:50.235386	Demo seed: Advise customers and manage real estate opportunities. Key skills: Real Estate Sales,Property Management,Negotiation,CRM. Job type: Full-time.	Entry	Full-time	Ha Noi	2026-03-30 15:16:50.235386	11000000.00	Property Consultant Entry #211	107	open	7000000	15000000	VND	2026-04-29 15:16:50.235386	\N	REAL-ESTATE	https://placehold.co/640x360/EFF6FF/2563EB?text=TTJobs
13	2026-06-03 15:16:50.235386	Demo seed: Build and maintain production software for growing product teams. Key skills: Java,Spring Boot,React,SQL,Docker. Job type: Full-time.	Entry	Full-time	Ha Noi	2026-04-14 15:16:50.235386	14000000.00	Software Engineer Entry #241	107	open	10000000	18000000	VND	2026-04-29 15:16:50.235386	\N	INFORMATION-TECHNOLOGY	https://placehold.co/640x360/EFF6FF/2563EB?text=TTJobs
14	2026-05-19 15:16:50.235386	Demo seed: Advise customers and manage real estate opportunities. Key skills: Real Estate Sales,Property Management,Negotiation,CRM. Job type: Full-time.	Entry	Full-time	Ha Noi	2026-04-29 15:16:50.235386	17000000.00	Property Consultant Entry #271	107	open	13000000	21000000	VND	2026-04-29 15:16:50.235386	\N	REAL-ESTATE	https://placehold.co/640x360/EFF6FF/2563EB?text=TTJobs
15	2026-05-20 15:16:50.235386	Demo seed: Own sales pipeline and close new business opportunities. Key skills: Sales,CRM,Negotiation,Customer Success. Job type: Part-time.	Junior	Part-time	Ho Chi Minh City	2026-04-28 15:16:50.235386	14500000.00	Sales Executive Junior #002	108	open	10500000	18500000	VND	2026-04-29 15:16:50.235386	\N	SALES	https://placehold.co/640x360/EFF6FF/2563EB?text=TTJobs
24	2026-05-20 15:16:50.235386	Demo seed: Handle accounting records, reports and compliance tasks. Key skills: Accounting,Tax,Audit,Excel. Job type: Part-time.	Junior	Part-time	Ho Chi Minh City	2026-04-28 15:16:50.235386	20500000.00	Accountant Junior #272	108	open	16500000	24500000	VND	2026-04-29 15:16:50.235386	\N	ACCOUNTING	https://placehold.co/640x360/EFF6FF/2563EB?text=TTJobs
25	2026-05-21 15:16:50.235386	Demo seed: Plan and execute digital campaigns across key channels. Key skills: SEO,Content Marketing,Google Ads,Social Media. Job type: Remote.	Mid	Remote	Da Nang	2026-04-27 15:16:50.235386	20000000.00	Digital Marketing Specialist Mid #003	109	open	16000000	24000000	VND	2026-04-29 15:16:50.235386	\N	MARKETING	https://placehold.co/640x360/EFF6FF/2563EB?text=TTJobs
26	2026-06-20 15:16:50.235386	Demo seed: Design thoughtful user experiences for web and mobile products. Key skills: Figma,UI Design,UX Research,Communication. Job type: Remote.	Mid	Remote	Da Nang	2026-03-28 15:16:50.235386	23000000.00	Product Designer Mid #033	109	open	19000000	27000000	VND	2026-04-29 15:16:50.235386	\N	DESIGN	https://placehold.co/640x360/EFF6FF/2563EB?text=TTJobs
27	2026-06-05 15:16:50.235386	Demo seed: Plan and execute digital campaigns across key channels. Key skills: SEO,Content Marketing,Google Ads,Social Media. Job type: Remote.	Mid	Remote	Da Nang	2026-04-12 15:16:50.235386	26000000.00	Digital Marketing Specialist Mid #063	109	open	22000000	30000000	VND	2026-04-29 15:16:50.235386	\N	MARKETING	https://placehold.co/640x360/EFF6FF/2563EB?text=TTJobs
28	2026-05-21 15:16:50.235386	Demo seed: Design thoughtful user experiences for web and mobile products. Key skills: Figma,UI Design,UX Research,Communication. Job type: Remote.	Mid	Remote	Da Nang	2026-04-27 15:16:50.235386	18500000.00	Product Designer Mid #093	109	open	14500000	22500000	VND	2026-04-29 15:16:50.235386	\N	DESIGN	https://placehold.co/640x360/EFF6FF/2563EB?text=TTJobs
29	2026-06-20 15:16:50.235386	Demo seed: Plan and execute digital campaigns across key channels. Key skills: SEO,Content Marketing,Google Ads,Social Media. Job type: Remote.	Mid	Remote	Da Nang	2026-03-28 15:16:50.235386	21500000.00	Digital Marketing Specialist Mid #123	109	open	17500000	25500000	VND	2026-04-29 15:16:50.235386	\N	MARKETING	https://placehold.co/640x360/EFF6FF/2563EB?text=TTJobs
30	2026-06-05 15:16:50.235386	Demo seed: Design thoughtful user experiences for web and mobile products. Key skills: Figma,UI Design,UX Research,Communication. Job type: Remote.	Mid	Remote	Da Nang	2026-04-12 15:16:50.235386	24500000.00	Product Designer Mid #153	109	open	20500000	28500000	VND	2026-04-29 15:16:50.235386	\N	DESIGN	https://placehold.co/640x360/EFF6FF/2563EB?text=TTJobs
31	2026-05-21 15:16:50.235386	Demo seed: Plan and execute digital campaigns across key channels. Key skills: SEO,Content Marketing,Google Ads,Social Media. Job type: Remote.	Mid	Remote	Da Nang	2026-04-27 15:16:50.235386	17000000.00	Digital Marketing Specialist Mid #183	109	open	13000000	21000000	VND	2026-04-29 15:16:50.235386	\N	MARKETING	https://placehold.co/640x360/EFF6FF/2563EB?text=TTJobs
32	2026-06-20 15:16:50.235386	Demo seed: Design thoughtful user experiences for web and mobile products. Key skills: Figma,UI Design,UX Research,Communication. Job type: Remote.	Mid	Remote	Da Nang	2026-03-28 15:16:50.235386	20000000.00	Product Designer Mid #213	109	open	16000000	24000000	VND	2026-04-29 15:16:50.235386	\N	DESIGN	https://placehold.co/640x360/EFF6FF/2563EB?text=TTJobs
33	2026-06-05 15:16:50.235386	Demo seed: Plan and execute digital campaigns across key channels. Key skills: SEO,Content Marketing,Google Ads,Social Media. Job type: Remote.	Mid	Remote	Da Nang	2026-04-12 15:16:50.235386	23000000.00	Digital Marketing Specialist Mid #243	109	open	19000000	27000000	VND	2026-04-29 15:16:50.235386	\N	MARKETING	https://placehold.co/640x360/EFF6FF/2563EB?text=TTJobs
34	2026-05-21 15:16:50.235386	Demo seed: Design thoughtful user experiences for web and mobile products. Key skills: Figma,UI Design,UX Research,Communication. Job type: Remote.	Mid	Remote	Da Nang	2026-04-27 15:16:50.235386	26000000.00	Product Designer Mid #273	109	open	22000000	30000000	VND	2026-04-29 15:16:50.235386	\N	DESIGN	https://placehold.co/640x360/EFF6FF/2563EB?text=TTJobs
35	2026-05-22 15:16:50.235386	Demo seed: Support hiring, onboarding and people operations. Key skills: Recruitment,HR Operations,Payroll,Communication. Job type: Hybrid.	Senior	Hybrid	Remote	2026-04-26 15:16:50.235386	27500000.00	HR Operations Specialist Senior #004	110	open	23500000	31500000	VND	2026-04-29 15:16:50.235386	\N	HR	https://placehold.co/640x360/EFF6FF/2563EB?text=TTJobs
36	2026-06-21 15:16:50.235386	Demo seed: Develop partnerships and new growth opportunities. Key skills: Partnerships,Market Research,Business Strategy,Negotiation. Job type: Hybrid.	Senior	Hybrid	Remote	2026-03-27 15:16:50.235386	30500000.00	Business Development Executive Senior #034	110	open	26500000	34500000	VND	2026-04-29 15:16:50.235386	\N	BUSINESS-DEVELOPMENT	https://placehold.co/640x360/EFF6FF/2563EB?text=TTJobs
37	2026-06-06 15:16:50.235386	Demo seed: Support hiring, onboarding and people operations. Key skills: Recruitment,HR Operations,Payroll,Communication. Job type: Hybrid.	Senior	Hybrid	Remote	2026-04-11 15:16:50.235386	23000000.00	HR Operations Specialist Senior #064	110	open	19000000	27000000	VND	2026-04-29 15:16:50.235386	\N	HR	https://placehold.co/640x360/EFF6FF/2563EB?text=TTJobs
38	2026-05-22 15:16:50.235386	Demo seed: Develop partnerships and new growth opportunities. Key skills: Partnerships,Market Research,Business Strategy,Negotiation. Job type: Hybrid.	Senior	Hybrid	Remote	2026-04-26 15:16:50.235386	26000000.00	Business Development Executive Senior #094	110	open	22000000	30000000	VND	2026-04-29 15:16:50.235386	\N	BUSINESS-DEVELOPMENT	https://placehold.co/640x360/EFF6FF/2563EB?text=TTJobs
39	2026-06-21 15:16:50.235386	Demo seed: Support hiring, onboarding and people operations. Key skills: Recruitment,HR Operations,Payroll,Communication. Job type: Hybrid.	Senior	Hybrid	Remote	2026-03-27 15:16:50.235386	29000000.00	HR Operations Specialist Senior #124	110	open	25000000	33000000	VND	2026-04-29 15:16:50.235386	\N	HR	https://placehold.co/640x360/EFF6FF/2563EB?text=TTJobs
40	2026-06-06 15:16:50.235386	Demo seed: Develop partnerships and new growth opportunities. Key skills: Partnerships,Market Research,Business Strategy,Negotiation. Job type: Hybrid.	Senior	Hybrid	Remote	2026-04-11 15:16:50.235386	32000000.00	Business Development Executive Senior #154	110	open	28000000	36000000	VND	2026-04-29 15:16:50.235386	\N	BUSINESS-DEVELOPMENT	https://placehold.co/640x360/EFF6FF/2563EB?text=TTJobs
41	2026-05-22 15:16:50.235386	Demo seed: Support hiring, onboarding and people operations. Key skills: Recruitment,HR Operations,Payroll,Communication. Job type: Hybrid.	Senior	Hybrid	Remote	2026-04-26 15:16:50.235386	24500000.00	HR Operations Specialist Senior #184	110	open	20500000	28500000	VND	2026-04-29 15:16:50.235386	\N	HR	https://placehold.co/640x360/EFF6FF/2563EB?text=TTJobs
42	2026-06-21 15:16:50.235386	Demo seed: Develop partnerships and new growth opportunities. Key skills: Partnerships,Market Research,Business Strategy,Negotiation. Job type: Hybrid.	Senior	Hybrid	Remote	2026-03-27 15:16:50.235386	27500000.00	Business Development Executive Senior #214	110	open	23500000	31500000	VND	2026-04-29 15:16:50.235386	\N	BUSINESS-DEVELOPMENT	https://placehold.co/640x360/EFF6FF/2563EB?text=TTJobs
43	2026-06-06 15:16:50.235386	Demo seed: Support hiring, onboarding and people operations. Key skills: Recruitment,HR Operations,Payroll,Communication. Job type: Hybrid.	Senior	Hybrid	Remote	2026-04-11 15:16:50.235386	30500000.00	HR Operations Specialist Senior #244	110	open	26500000	34500000	VND	2026-04-29 15:16:50.235386	\N	HR	https://placehold.co/640x360/EFF6FF/2563EB?text=TTJobs
44	2026-05-22 15:16:50.235386	Demo seed: Develop partnerships and new growth opportunities. Key skills: Partnerships,Market Research,Business Strategy,Negotiation. Job type: Hybrid.	Senior	Hybrid	Remote	2026-04-26 15:16:50.235386	23000000.00	Business Development Executive Senior #274	110	open	19000000	27000000	VND	2026-04-29 15:16:50.235386	\N	BUSINESS-DEVELOPMENT	https://placehold.co/640x360/EFF6FF/2563EB?text=TTJobs
45	2026-05-23 15:16:50.235386	Demo seed: Analyze financial performance and support planning decisions. Key skills: Financial Analysis,Risk Management,Excel,SQL. Job type: Contract.	Lead	Contract	Hybrid	2026-04-25 15:16:50.235386	35000000.00	Financial Analyst Lead #005	111	open	31000000	39000000	VND	2026-04-29 15:16:50.235386	\N	FINANCE	https://placehold.co/640x360/EFF6FF/2563EB?text=TTJobs
46	2026-06-22 15:16:50.235386	Demo seed: Design practical technical solutions for customer and internal needs. Key skills: Python,AWS,Docker,SQL. Job type: Contract.	Lead	Contract	Hybrid	2026-03-26 15:16:50.235386	38000000.00	Solutions Engineer Lead #035	111	open	34000000	42000000	VND	2026-04-29 15:16:50.235386	\N	ENGINEERING	https://placehold.co/640x360/EFF6FF/2563EB?text=TTJobs
47	2026-06-07 15:16:50.235386	Demo seed: Analyze financial performance and support planning decisions. Key skills: Financial Analysis,Risk Management,Excel,SQL. Job type: Contract.	Lead	Contract	Hybrid	2026-04-10 15:16:50.235386	30500000.00	Financial Analyst Lead #065	111	open	26500000	34500000	VND	2026-04-29 15:16:50.235386	\N	FINANCE	https://placehold.co/640x360/EFF6FF/2563EB?text=TTJobs
48	2026-05-23 15:16:50.235386	Demo seed: Design practical technical solutions for customer and internal needs. Key skills: Python,AWS,Docker,SQL. Job type: Contract.	Lead	Contract	Hybrid	2026-04-25 15:16:50.235386	33500000.00	Solutions Engineer Lead #095	111	open	29500000	37500000	VND	2026-04-29 15:16:50.235386	\N	ENGINEERING	https://placehold.co/640x360/EFF6FF/2563EB?text=TTJobs
49	2026-06-22 15:16:50.235386	Demo seed: Analyze financial performance and support planning decisions. Key skills: Financial Analysis,Risk Management,Excel,SQL. Job type: Contract.	Lead	Contract	Hybrid	2026-03-26 15:16:50.235386	36500000.00	Financial Analyst Lead #125	111	open	32500000	40500000	VND	2026-04-29 15:16:50.235386	\N	FINANCE	https://placehold.co/640x360/EFF6FF/2563EB?text=TTJobs
50	2026-06-07 15:16:50.235386	Demo seed: Design practical technical solutions for customer and internal needs. Key skills: Python,AWS,Docker,SQL. Job type: Contract.	Lead	Contract	Hybrid	2026-04-10 15:16:50.235386	29000000.00	Solutions Engineer Lead #155	111	open	25000000	33000000	VND	2026-04-29 15:16:50.235386	\N	ENGINEERING	https://placehold.co/640x360/EFF6FF/2563EB?text=TTJobs
51	2026-05-23 15:16:50.235386	Demo seed: Analyze financial performance and support planning decisions. Key skills: Financial Analysis,Risk Management,Excel,SQL. Job type: Contract.	Lead	Contract	Hybrid	2026-04-25 15:16:50.235386	32000000.00	Financial Analyst Lead #185	111	open	28000000	36000000	VND	2026-04-29 15:16:50.235386	\N	FINANCE	https://placehold.co/640x360/EFF6FF/2563EB?text=TTJobs
52	2026-06-22 15:16:50.235386	Demo seed: Design practical technical solutions for customer and internal needs. Key skills: Python,AWS,Docker,SQL. Job type: Contract.	Lead	Contract	Hybrid	2026-03-26 15:16:50.235386	35000000.00	Solutions Engineer Lead #215	111	open	31000000	39000000	VND	2026-04-29 15:16:50.235386	\N	ENGINEERING	https://placehold.co/640x360/EFF6FF/2563EB?text=TTJobs
53	2026-06-07 15:16:50.235386	Demo seed: Analyze financial performance and support planning decisions. Key skills: Financial Analysis,Risk Management,Excel,SQL. Job type: Contract.	Lead	Contract	Hybrid	2026-04-10 15:16:50.235386	38000000.00	Financial Analyst Lead #245	111	open	34000000	42000000	VND	2026-04-29 15:16:50.235386	\N	FINANCE	https://placehold.co/640x360/EFF6FF/2563EB?text=TTJobs
54	2026-05-23 15:16:50.235386	Demo seed: Design practical technical solutions for customer and internal needs. Key skills: Python,AWS,Docker,SQL. Job type: Contract.	Lead	Contract	Hybrid	2026-04-25 15:16:50.235386	30500000.00	Solutions Engineer Lead #275	111	open	26500000	34500000	VND	2026-04-29 15:16:50.235386	\N	ENGINEERING	https://placehold.co/640x360/EFF6FF/2563EB?text=TTJobs
55	2026-05-24 15:16:50.235386	Demo seed: Help customers resolve issues with clear and friendly support. Key skills: Customer Support,Communication,CRM,Customer Success. Job type: Internship.	Entry	Internship	Ha Noi	2026-04-24 15:16:50.235386	18500000.00	Customer Support Specialist Entry #006	112	open	14500000	22500000	VND	2026-04-29 15:16:50.235386	\N	CUSTOMER-SERVICE	https://placehold.co/640x360/EFF6FF/2563EB?text=TTJobs
56	2026-06-23 15:16:50.235386	Demo seed: Coordinate daily operations and improve team execution. Key skills: Excel,Communication,Customer Success,Business Strategy. Job type: Internship.	Entry	Internship	Ha Noi	2026-03-25 15:16:50.235386	11000000.00	Operations Coordinator Entry #036	112	open	7000000	15000000	VND	2026-04-29 15:16:50.235386	\N	OPERATIONS	https://placehold.co/640x360/EFF6FF/2563EB?text=TTJobs
57	2026-06-08 15:16:50.235386	Demo seed: Help customers resolve issues with clear and friendly support. Key skills: Customer Support,Communication,CRM,Customer Success. Job type: Internship.	Entry	Internship	Ha Noi	2026-04-09 15:16:50.235386	14000000.00	Customer Support Specialist Entry #066	112	open	10000000	18000000	VND	2026-04-29 15:16:50.235386	\N	CUSTOMER-SERVICE	https://placehold.co/640x360/EFF6FF/2563EB?text=TTJobs
58	2026-05-24 15:16:50.235386	Demo seed: Coordinate daily operations and improve team execution. Key skills: Excel,Communication,Customer Success,Business Strategy. Job type: Internship.	Entry	Internship	Ha Noi	2026-04-24 15:16:50.235386	17000000.00	Operations Coordinator Entry #096	112	open	13000000	21000000	VND	2026-04-29 15:16:50.235386	\N	OPERATIONS	https://placehold.co/640x360/EFF6FF/2563EB?text=TTJobs
59	2026-06-23 15:16:50.235386	Demo seed: Help customers resolve issues with clear and friendly support. Key skills: Customer Support,Communication,CRM,Customer Success. Job type: Internship.	Entry	Internship	Ha Noi	2026-03-25 15:16:50.235386	20000000.00	Customer Support Specialist Entry #126	112	open	16000000	24000000	VND	2026-04-29 15:16:50.235386	\N	CUSTOMER-SERVICE	https://placehold.co/640x360/EFF6FF/2563EB?text=TTJobs
60	2026-06-08 15:16:50.235386	Demo seed: Coordinate daily operations and improve team execution. Key skills: Excel,Communication,Customer Success,Business Strategy. Job type: Internship.	Entry	Internship	Ha Noi	2026-04-09 15:16:50.235386	12500000.00	Operations Coordinator Entry #156	112	open	8500000	16500000	VND	2026-04-29 15:16:50.235386	\N	OPERATIONS	https://placehold.co/640x360/EFF6FF/2563EB?text=TTJobs
61	2026-05-24 15:16:50.235386	Demo seed: Help customers resolve issues with clear and friendly support. Key skills: Customer Support,Communication,CRM,Customer Success. Job type: Internship.	Entry	Internship	Ha Noi	2026-04-24 15:16:50.235386	15500000.00	Customer Support Specialist Entry #186	112	open	11500000	19500000	VND	2026-04-29 15:16:50.235386	\N	CUSTOMER-SERVICE	https://placehold.co/640x360/EFF6FF/2563EB?text=TTJobs
62	2026-06-23 15:16:50.235386	Demo seed: Coordinate daily operations and improve team execution. Key skills: Excel,Communication,Customer Success,Business Strategy. Job type: Internship.	Entry	Internship	Ha Noi	2026-03-25 15:16:50.235386	18500000.00	Operations Coordinator Entry #216	112	open	14500000	22500000	VND	2026-04-29 15:16:50.235386	\N	OPERATIONS	https://placehold.co/640x360/EFF6FF/2563EB?text=TTJobs
63	2026-06-08 15:16:50.235386	Demo seed: Help customers resolve issues with clear and friendly support. Key skills: Customer Support,Communication,CRM,Customer Success. Job type: Internship.	Entry	Internship	Ha Noi	2026-04-09 15:16:50.235386	11000000.00	Customer Support Specialist Entry #246	112	open	7000000	15000000	VND	2026-04-29 15:16:50.235386	\N	CUSTOMER-SERVICE	https://placehold.co/640x360/EFF6FF/2563EB?text=TTJobs
64	2026-05-24 15:16:50.235386	Demo seed: Coordinate daily operations and improve team execution. Key skills: Excel,Communication,Customer Success,Business Strategy. Job type: Internship.	Entry	Internship	Ha Noi	2026-04-24 15:16:50.235386	14000000.00	Operations Coordinator Entry #276	112	open	10000000	18000000	VND	2026-04-29 15:16:50.235386	\N	OPERATIONS	https://placehold.co/640x360/EFF6FF/2563EB?text=TTJobs
65	2026-05-25 15:16:50.235386	Demo seed: Advise customers and manage real estate opportunities. Key skills: Real Estate Sales,Property Management,Negotiation,CRM. Job type: Full-time.	Junior	Full-time	Ho Chi Minh City	2026-04-23 15:16:50.235386	22000000.00	Property Consultant Junior #007	113	open	18000000	26000000	VND	2026-04-29 15:16:50.235386	\N	REAL-ESTATE	https://placehold.co/640x360/EFF6FF/2563EB?text=TTJobs
66	2026-06-24 15:16:50.235386	Demo seed: Build and maintain production software for growing product teams. Key skills: Java,Spring Boot,React,SQL,Docker. Job type: Full-time.	Junior	Full-time	Ho Chi Minh City	2026-03-24 15:16:50.235386	14500000.00	Software Engineer Junior #037	113	open	10500000	18500000	VND	2026-04-29 15:16:50.235386	\N	INFORMATION-TECHNOLOGY	https://placehold.co/640x360/EFF6FF/2563EB?text=TTJobs
67	2026-06-09 15:16:50.235386	Demo seed: Advise customers and manage real estate opportunities. Key skills: Real Estate Sales,Property Management,Negotiation,CRM. Job type: Full-time.	Junior	Full-time	Ho Chi Minh City	2026-04-08 15:16:50.235386	17500000.00	Property Consultant Junior #067	113	open	13500000	21500000	VND	2026-04-29 15:16:50.235386	\N	REAL-ESTATE	https://placehold.co/640x360/EFF6FF/2563EB?text=TTJobs
68	2026-05-25 15:16:50.235386	Demo seed: Build and maintain production software for growing product teams. Key skills: Java,Spring Boot,React,SQL,Docker. Job type: Full-time.	Junior	Full-time	Ho Chi Minh City	2026-04-23 15:16:50.235386	20500000.00	Software Engineer Junior #097	113	open	16500000	24500000	VND	2026-04-29 15:16:50.235386	\N	INFORMATION-TECHNOLOGY	https://placehold.co/640x360/EFF6FF/2563EB?text=TTJobs
69	2026-06-24 15:16:50.235386	Demo seed: Advise customers and manage real estate opportunities. Key skills: Real Estate Sales,Property Management,Negotiation,CRM. Job type: Full-time.	Junior	Full-time	Ho Chi Minh City	2026-03-24 15:16:50.235386	13000000.00	Property Consultant Junior #127	113	open	9000000	17000000	VND	2026-04-29 15:16:50.235386	\N	REAL-ESTATE	https://placehold.co/640x360/EFF6FF/2563EB?text=TTJobs
70	2026-06-09 15:16:50.235386	Demo seed: Build and maintain production software for growing product teams. Key skills: Java,Spring Boot,React,SQL,Docker. Job type: Full-time.	Junior	Full-time	Ho Chi Minh City	2026-04-08 15:16:50.235386	16000000.00	Software Engineer Junior #157	113	open	12000000	20000000	VND	2026-04-29 15:16:50.235386	\N	INFORMATION-TECHNOLOGY	https://placehold.co/640x360/EFF6FF/2563EB?text=TTJobs
71	2026-05-25 15:16:50.235386	Demo seed: Advise customers and manage real estate opportunities. Key skills: Real Estate Sales,Property Management,Negotiation,CRM. Job type: Full-time.	Junior	Full-time	Ho Chi Minh City	2026-04-23 15:16:50.235386	19000000.00	Property Consultant Junior #187	113	open	15000000	23000000	VND	2026-04-29 15:16:50.235386	\N	REAL-ESTATE	https://placehold.co/640x360/EFF6FF/2563EB?text=TTJobs
72	2026-06-24 15:16:50.235386	Demo seed: Build and maintain production software for growing product teams. Key skills: Java,Spring Boot,React,SQL,Docker. Job type: Full-time.	Junior	Full-time	Ho Chi Minh City	2026-03-24 15:16:50.235386	22000000.00	Software Engineer Junior #217	113	open	18000000	26000000	VND	2026-04-29 15:16:50.235386	\N	INFORMATION-TECHNOLOGY	https://placehold.co/640x360/EFF6FF/2563EB?text=TTJobs
73	2026-06-09 15:16:50.235386	Demo seed: Advise customers and manage real estate opportunities. Key skills: Real Estate Sales,Property Management,Negotiation,CRM. Job type: Full-time.	Junior	Full-time	Ho Chi Minh City	2026-04-08 15:16:50.235386	14500000.00	Property Consultant Junior #247	113	open	10500000	18500000	VND	2026-04-29 15:16:50.235386	\N	REAL-ESTATE	https://placehold.co/640x360/EFF6FF/2563EB?text=TTJobs
74	2026-05-25 15:16:50.235386	Demo seed: Build and maintain production software for growing product teams. Key skills: Java,Spring Boot,React,SQL,Docker. Job type: Full-time.	Junior	Full-time	Ho Chi Minh City	2026-04-23 15:16:50.235386	17500000.00	Software Engineer Junior #277	113	open	13500000	21500000	VND	2026-04-29 15:16:50.235386	\N	INFORMATION-TECHNOLOGY	https://placehold.co/640x360/EFF6FF/2563EB?text=TTJobs
75	2026-05-26 15:16:50.235386	Demo seed: Handle accounting records, reports and compliance tasks. Key skills: Accounting,Tax,Audit,Excel. Job type: Part-time.	Mid	Part-time	Da Nang	2026-04-22 15:16:50.235386	17000000.00	Accountant Mid #008	114	open	13000000	21000000	VND	2026-04-29 15:16:50.235386	\N	ACCOUNTING	https://placehold.co/640x360/EFF6FF/2563EB?text=TTJobs
76	2026-06-25 15:16:50.235386	Demo seed: Own sales pipeline and close new business opportunities. Key skills: Sales,CRM,Negotiation,Customer Success. Job type: Part-time.	Mid	Part-time	Da Nang	2026-03-23 15:16:50.235386	20000000.00	Sales Executive Mid #038	114	open	16000000	24000000	VND	2026-04-29 15:16:50.235386	\N	SALES	https://placehold.co/640x360/EFF6FF/2563EB?text=TTJobs
77	2026-06-10 15:16:50.235386	Demo seed: Handle accounting records, reports and compliance tasks. Key skills: Accounting,Tax,Audit,Excel. Job type: Part-time.	Mid	Part-time	Da Nang	2026-04-07 15:16:50.235386	23000000.00	Accountant Mid #068	114	open	19000000	27000000	VND	2026-04-29 15:16:50.235386	\N	ACCOUNTING	https://placehold.co/640x360/EFF6FF/2563EB?text=TTJobs
78	2026-05-26 15:16:50.235386	Demo seed: Own sales pipeline and close new business opportunities. Key skills: Sales,CRM,Negotiation,Customer Success. Job type: Part-time.	Mid	Part-time	Da Nang	2026-04-22 15:16:50.235386	26000000.00	Sales Executive Mid #098	114	open	22000000	30000000	VND	2026-04-29 15:16:50.235386	\N	SALES	https://placehold.co/640x360/EFF6FF/2563EB?text=TTJobs
79	2026-06-25 15:16:50.235386	Demo seed: Handle accounting records, reports and compliance tasks. Key skills: Accounting,Tax,Audit,Excel. Job type: Part-time.	Mid	Part-time	Da Nang	2026-03-23 15:16:50.235386	18500000.00	Accountant Mid #128	114	open	14500000	22500000	VND	2026-04-29 15:16:50.235386	\N	ACCOUNTING	https://placehold.co/640x360/EFF6FF/2563EB?text=TTJobs
80	2026-06-10 15:16:50.235386	Demo seed: Own sales pipeline and close new business opportunities. Key skills: Sales,CRM,Negotiation,Customer Success. Job type: Part-time.	Mid	Part-time	Da Nang	2026-04-07 15:16:50.235386	21500000.00	Sales Executive Mid #158	114	open	17500000	25500000	VND	2026-04-29 15:16:50.235386	\N	SALES	https://placehold.co/640x360/EFF6FF/2563EB?text=TTJobs
81	2026-05-26 15:16:50.235386	Demo seed: Handle accounting records, reports and compliance tasks. Key skills: Accounting,Tax,Audit,Excel. Job type: Part-time.	Mid	Part-time	Da Nang	2026-04-22 15:16:50.235386	24500000.00	Accountant Mid #188	114	open	20500000	28500000	VND	2026-04-29 15:16:50.235386	\N	ACCOUNTING	https://placehold.co/640x360/EFF6FF/2563EB?text=TTJobs
82	2026-06-25 15:16:50.235386	Demo seed: Own sales pipeline and close new business opportunities. Key skills: Sales,CRM,Negotiation,Customer Success. Job type: Part-time.	Mid	Part-time	Da Nang	2026-03-23 15:16:50.235386	17000000.00	Sales Executive Mid #218	114	open	13000000	21000000	VND	2026-04-29 15:16:50.235386	\N	SALES	https://placehold.co/640x360/EFF6FF/2563EB?text=TTJobs
83	2026-06-10 15:16:50.235386	Demo seed: Handle accounting records, reports and compliance tasks. Key skills: Accounting,Tax,Audit,Excel. Job type: Part-time.	Mid	Part-time	Da Nang	2026-04-07 15:16:50.235386	20000000.00	Accountant Mid #248	114	open	16000000	24000000	VND	2026-04-29 15:16:50.235386	\N	ACCOUNTING	https://placehold.co/640x360/EFF6FF/2563EB?text=TTJobs
84	2026-05-26 15:16:50.235386	Demo seed: Own sales pipeline and close new business opportunities. Key skills: Sales,CRM,Negotiation,Customer Success. Job type: Part-time.	Mid	Part-time	Da Nang	2026-04-22 15:16:50.235386	23000000.00	Sales Executive Mid #278	114	open	19000000	27000000	VND	2026-04-29 15:16:50.235386	\N	SALES	https://placehold.co/640x360/EFF6FF/2563EB?text=TTJobs
85	2026-05-27 15:16:50.235386	Demo seed: Design thoughtful user experiences for web and mobile products. Key skills: Figma,UI Design,UX Research,Communication. Job type: Remote.	Senior	Remote	Remote	2026-04-21 15:16:50.235386	24500000.00	Product Designer Senior #009	115	open	20500000	28500000	VND	2026-04-29 15:16:50.235386	\N	DESIGN	https://placehold.co/640x360/EFF6FF/2563EB?text=TTJobs
86	2026-06-26 15:16:50.235386	Demo seed: Plan and execute digital campaigns across key channels. Key skills: SEO,Content Marketing,Google Ads,Social Media. Job type: Remote.	Senior	Remote	Remote	2026-03-22 15:16:50.235386	27500000.00	Digital Marketing Specialist Senior #039	115	open	23500000	31500000	VND	2026-04-29 15:16:50.235386	\N	MARKETING	https://placehold.co/640x360/EFF6FF/2563EB?text=TTJobs
87	2026-06-11 15:16:50.235386	Demo seed: Design thoughtful user experiences for web and mobile products. Key skills: Figma,UI Design,UX Research,Communication. Job type: Remote.	Senior	Remote	Remote	2026-04-06 15:16:50.235386	30500000.00	Product Designer Senior #069	115	open	26500000	34500000	VND	2026-04-29 15:16:50.235386	\N	DESIGN	https://placehold.co/640x360/EFF6FF/2563EB?text=TTJobs
88	2026-05-27 15:16:50.235386	Demo seed: Plan and execute digital campaigns across key channels. Key skills: SEO,Content Marketing,Google Ads,Social Media. Job type: Remote.	Senior	Remote	Remote	2026-04-21 15:16:50.235386	23000000.00	Digital Marketing Specialist Senior #099	115	open	19000000	27000000	VND	2026-04-29 15:16:50.235386	\N	MARKETING	https://placehold.co/640x360/EFF6FF/2563EB?text=TTJobs
89	2026-06-26 15:16:50.235386	Demo seed: Design thoughtful user experiences for web and mobile products. Key skills: Figma,UI Design,UX Research,Communication. Job type: Remote.	Senior	Remote	Remote	2026-03-22 15:16:50.235386	26000000.00	Product Designer Senior #129	115	open	22000000	30000000	VND	2026-04-29 15:16:50.235386	\N	DESIGN	https://placehold.co/640x360/EFF6FF/2563EB?text=TTJobs
90	2026-06-11 15:16:50.235386	Demo seed: Plan and execute digital campaigns across key channels. Key skills: SEO,Content Marketing,Google Ads,Social Media. Job type: Remote.	Senior	Remote	Remote	2026-04-06 15:16:50.235386	29000000.00	Digital Marketing Specialist Senior #159	115	open	25000000	33000000	VND	2026-04-29 15:16:50.235386	\N	MARKETING	https://placehold.co/640x360/EFF6FF/2563EB?text=TTJobs
91	2026-05-27 15:16:50.235386	Demo seed: Design thoughtful user experiences for web and mobile products. Key skills: Figma,UI Design,UX Research,Communication. Job type: Remote.	Senior	Remote	Remote	2026-04-21 15:16:50.235386	32000000.00	Product Designer Senior #189	115	open	28000000	36000000	VND	2026-04-29 15:16:50.235386	\N	DESIGN	https://placehold.co/640x360/EFF6FF/2563EB?text=TTJobs
92	2026-06-26 15:16:50.235386	Demo seed: Plan and execute digital campaigns across key channels. Key skills: SEO,Content Marketing,Google Ads,Social Media. Job type: Remote.	Senior	Remote	Remote	2026-03-22 15:16:50.235386	24500000.00	Digital Marketing Specialist Senior #219	115	open	20500000	28500000	VND	2026-04-29 15:16:50.235386	\N	MARKETING	https://placehold.co/640x360/EFF6FF/2563EB?text=TTJobs
93	2026-06-11 15:16:50.235386	Demo seed: Design thoughtful user experiences for web and mobile products. Key skills: Figma,UI Design,UX Research,Communication. Job type: Remote.	Senior	Remote	Remote	2026-04-06 15:16:50.235386	27500000.00	Product Designer Senior #249	115	open	23500000	31500000	VND	2026-04-29 15:16:50.235386	\N	DESIGN	https://placehold.co/640x360/EFF6FF/2563EB?text=TTJobs
94	2026-05-27 15:16:50.235386	Demo seed: Plan and execute digital campaigns across key channels. Key skills: SEO,Content Marketing,Google Ads,Social Media. Job type: Remote.	Senior	Remote	Remote	2026-04-21 15:16:50.235386	30500000.00	Digital Marketing Specialist Senior #279	115	open	26500000	34500000	VND	2026-04-29 15:16:50.235386	\N	MARKETING	https://placehold.co/640x360/EFF6FF/2563EB?text=TTJobs
95	2026-05-28 15:16:50.235386	Demo seed: Develop partnerships and new growth opportunities. Key skills: Partnerships,Market Research,Business Strategy,Negotiation. Job type: Hybrid.	Lead	Hybrid	Hybrid	2026-04-20 15:16:50.235386	32000000.00	Business Development Executive Lead #010	116	open	28000000	36000000	VND	2026-04-29 15:16:50.235386	\N	BUSINESS-DEVELOPMENT	https://placehold.co/640x360/EFF6FF/2563EB?text=TTJobs
96	2026-06-27 15:16:50.235386	Demo seed: Support hiring, onboarding and people operations. Key skills: Recruitment,HR Operations,Payroll,Communication. Job type: Hybrid.	Lead	Hybrid	Hybrid	2026-03-21 15:16:50.235386	35000000.00	HR Operations Specialist Lead #040	116	open	31000000	39000000	VND	2026-04-29 15:16:50.235386	\N	HR	https://placehold.co/640x360/EFF6FF/2563EB?text=TTJobs
97	2026-06-12 15:16:50.235386	Demo seed: Develop partnerships and new growth opportunities. Key skills: Partnerships,Market Research,Business Strategy,Negotiation. Job type: Hybrid.	Lead	Hybrid	Hybrid	2026-04-05 15:16:50.235386	38000000.00	Business Development Executive Lead #070	116	open	34000000	42000000	VND	2026-04-29 15:16:50.235386	\N	BUSINESS-DEVELOPMENT	https://placehold.co/640x360/EFF6FF/2563EB?text=TTJobs
98	2026-05-28 15:16:50.235386	Demo seed: Support hiring, onboarding and people operations. Key skills: Recruitment,HR Operations,Payroll,Communication. Job type: Hybrid.	Lead	Hybrid	Hybrid	2026-04-20 15:16:50.235386	30500000.00	HR Operations Specialist Lead #100	116	open	26500000	34500000	VND	2026-04-29 15:16:50.235386	\N	HR	https://placehold.co/640x360/EFF6FF/2563EB?text=TTJobs
99	2026-06-27 15:16:50.235386	Demo seed: Develop partnerships and new growth opportunities. Key skills: Partnerships,Market Research,Business Strategy,Negotiation. Job type: Hybrid.	Lead	Hybrid	Hybrid	2026-03-21 15:16:50.235386	33500000.00	Business Development Executive Lead #130	116	open	29500000	37500000	VND	2026-04-29 15:16:50.235386	\N	BUSINESS-DEVELOPMENT	https://placehold.co/640x360/EFF6FF/2563EB?text=TTJobs
100	2026-06-12 15:16:50.235386	Demo seed: Support hiring, onboarding and people operations. Key skills: Recruitment,HR Operations,Payroll,Communication. Job type: Hybrid.	Lead	Hybrid	Hybrid	2026-04-05 15:16:50.235386	36500000.00	HR Operations Specialist Lead #160	116	open	32500000	40500000	VND	2026-04-29 15:16:50.235386	\N	HR	https://placehold.co/640x360/EFF6FF/2563EB?text=TTJobs
101	2026-05-28 15:16:50.235386	Demo seed: Develop partnerships and new growth opportunities. Key skills: Partnerships,Market Research,Business Strategy,Negotiation. Job type: Hybrid.	Lead	Hybrid	Hybrid	2026-04-20 15:16:50.235386	29000000.00	Business Development Executive Lead #190	116	open	25000000	33000000	VND	2026-04-29 15:16:50.235386	\N	BUSINESS-DEVELOPMENT	https://placehold.co/640x360/EFF6FF/2563EB?text=TTJobs
102	2026-06-27 15:16:50.235386	Demo seed: Support hiring, onboarding and people operations. Key skills: Recruitment,HR Operations,Payroll,Communication. Job type: Hybrid.	Lead	Hybrid	Hybrid	2026-03-21 15:16:50.235386	32000000.00	HR Operations Specialist Lead #220	116	open	28000000	36000000	VND	2026-04-29 15:16:50.235386	\N	HR	https://placehold.co/640x360/EFF6FF/2563EB?text=TTJobs
103	2026-06-12 15:16:50.235386	Demo seed: Develop partnerships and new growth opportunities. Key skills: Partnerships,Market Research,Business Strategy,Negotiation. Job type: Hybrid.	Lead	Hybrid	Hybrid	2026-04-05 15:16:50.235386	35000000.00	Business Development Executive Lead #250	116	open	31000000	39000000	VND	2026-04-29 15:16:50.235386	\N	BUSINESS-DEVELOPMENT	https://placehold.co/640x360/EFF6FF/2563EB?text=TTJobs
104	2026-05-28 15:16:50.235386	Demo seed: Support hiring, onboarding and people operations. Key skills: Recruitment,HR Operations,Payroll,Communication. Job type: Hybrid.	Lead	Hybrid	Hybrid	2026-04-20 15:16:50.235386	38000000.00	HR Operations Specialist Lead #280	116	open	34000000	42000000	VND	2026-04-29 15:16:50.235386	\N	HR	https://placehold.co/640x360/EFF6FF/2563EB?text=TTJobs
105	2026-05-29 15:16:50.235386	Demo seed: Design practical technical solutions for customer and internal needs. Key skills: Python,AWS,Docker,SQL. Job type: Contract.	Entry	Contract	Ha Noi	2026-04-19 15:16:50.235386	15500000.00	Solutions Engineer Entry #011	117	open	11500000	19500000	VND	2026-04-29 15:16:50.235386	\N	ENGINEERING	https://placehold.co/640x360/EFF6FF/2563EB?text=TTJobs
106	2026-06-28 15:16:50.235386	Demo seed: Analyze financial performance and support planning decisions. Key skills: Financial Analysis,Risk Management,Excel,SQL. Job type: Contract.	Entry	Contract	Ha Noi	2026-03-20 15:16:50.235386	18500000.00	Financial Analyst Entry #041	117	open	14500000	22500000	VND	2026-04-29 15:16:50.235386	\N	FINANCE	https://placehold.co/640x360/EFF6FF/2563EB?text=TTJobs
107	2026-06-13 15:16:50.235386	Demo seed: Design practical technical solutions for customer and internal needs. Key skills: Python,AWS,Docker,SQL. Job type: Contract.	Entry	Contract	Ha Noi	2026-04-04 15:16:50.235386	11000000.00	Solutions Engineer Entry #071	117	open	7000000	15000000	VND	2026-04-29 15:16:50.235386	\N	ENGINEERING	https://placehold.co/640x360/EFF6FF/2563EB?text=TTJobs
108	2026-05-29 15:16:50.235386	Demo seed: Analyze financial performance and support planning decisions. Key skills: Financial Analysis,Risk Management,Excel,SQL. Job type: Contract.	Entry	Contract	Ha Noi	2026-04-19 15:16:50.235386	14000000.00	Financial Analyst Entry #101	117	open	10000000	18000000	VND	2026-04-29 15:16:50.235386	\N	FINANCE	https://placehold.co/640x360/EFF6FF/2563EB?text=TTJobs
109	2026-06-28 15:16:50.235386	Demo seed: Design practical technical solutions for customer and internal needs. Key skills: Python,AWS,Docker,SQL. Job type: Contract.	Entry	Contract	Ha Noi	2026-03-20 15:16:50.235386	17000000.00	Solutions Engineer Entry #131	117	open	13000000	21000000	VND	2026-04-29 15:16:50.235386	\N	ENGINEERING	https://placehold.co/640x360/EFF6FF/2563EB?text=TTJobs
110	2026-06-13 15:16:50.235386	Demo seed: Analyze financial performance and support planning decisions. Key skills: Financial Analysis,Risk Management,Excel,SQL. Job type: Contract.	Entry	Contract	Ha Noi	2026-04-04 15:16:50.235386	20000000.00	Financial Analyst Entry #161	117	open	16000000	24000000	VND	2026-04-29 15:16:50.235386	\N	FINANCE	https://placehold.co/640x360/EFF6FF/2563EB?text=TTJobs
111	2026-05-29 15:16:50.235386	Demo seed: Design practical technical solutions for customer and internal needs. Key skills: Python,AWS,Docker,SQL. Job type: Contract.	Entry	Contract	Ha Noi	2026-04-19 15:16:50.235386	12500000.00	Solutions Engineer Entry #191	117	open	8500000	16500000	VND	2026-04-29 15:16:50.235386	\N	ENGINEERING	https://placehold.co/640x360/EFF6FF/2563EB?text=TTJobs
112	2026-06-28 15:16:50.235386	Demo seed: Analyze financial performance and support planning decisions. Key skills: Financial Analysis,Risk Management,Excel,SQL. Job type: Contract.	Entry	Contract	Ha Noi	2026-03-20 15:16:50.235386	15500000.00	Financial Analyst Entry #221	117	open	11500000	19500000	VND	2026-04-29 15:16:50.235386	\N	FINANCE	https://placehold.co/640x360/EFF6FF/2563EB?text=TTJobs
113	2026-06-13 15:16:50.235386	Demo seed: Design practical technical solutions for customer and internal needs. Key skills: Python,AWS,Docker,SQL. Job type: Contract.	Entry	Contract	Ha Noi	2026-04-04 15:16:50.235386	18500000.00	Solutions Engineer Entry #251	117	open	14500000	22500000	VND	2026-04-29 15:16:50.235386	\N	ENGINEERING	https://placehold.co/640x360/EFF6FF/2563EB?text=TTJobs
114	2026-05-29 15:16:50.235386	Demo seed: Analyze financial performance and support planning decisions. Key skills: Financial Analysis,Risk Management,Excel,SQL. Job type: Contract.	Entry	Contract	Ha Noi	2026-04-19 15:16:50.235386	11000000.00	Financial Analyst Entry #281	117	open	7000000	15000000	VND	2026-04-29 15:16:50.235386	\N	FINANCE	https://placehold.co/640x360/EFF6FF/2563EB?text=TTJobs
115	2026-05-30 15:16:50.235386	Demo seed: Coordinate daily operations and improve team execution. Key skills: Excel,Communication,Customer Success,Business Strategy. Job type: Internship.	Junior	Internship	Ho Chi Minh City	2026-04-18 15:16:50.235386	19000000.00	Operations Coordinator Junior #012	118	open	15000000	23000000	VND	2026-04-29 15:16:50.235386	\N	OPERATIONS	https://placehold.co/640x360/EFF6FF/2563EB?text=TTJobs
116	2026-06-29 15:16:50.235386	Demo seed: Help customers resolve issues with clear and friendly support. Key skills: Customer Support,Communication,CRM,Customer Success. Job type: Internship.	Junior	Internship	Ho Chi Minh City	2026-03-19 15:16:50.235386	22000000.00	Customer Support Specialist Junior #042	118	open	18000000	26000000	VND	2026-04-29 15:16:50.235386	\N	CUSTOMER-SERVICE	https://placehold.co/640x360/EFF6FF/2563EB?text=TTJobs
117	2026-06-14 15:16:50.235386	Demo seed: Coordinate daily operations and improve team execution. Key skills: Excel,Communication,Customer Success,Business Strategy. Job type: Internship.	Junior	Internship	Ho Chi Minh City	2026-04-03 15:16:50.235386	14500000.00	Operations Coordinator Junior #072	118	open	10500000	18500000	VND	2026-04-29 15:16:50.235386	\N	OPERATIONS	https://placehold.co/640x360/EFF6FF/2563EB?text=TTJobs
118	2026-05-30 15:16:50.235386	Demo seed: Help customers resolve issues with clear and friendly support. Key skills: Customer Support,Communication,CRM,Customer Success. Job type: Internship.	Junior	Internship	Ho Chi Minh City	2026-04-18 15:16:50.235386	17500000.00	Customer Support Specialist Junior #102	118	open	13500000	21500000	VND	2026-04-29 15:16:50.235386	\N	CUSTOMER-SERVICE	https://placehold.co/640x360/EFF6FF/2563EB?text=TTJobs
119	2026-06-29 15:16:50.235386	Demo seed: Coordinate daily operations and improve team execution. Key skills: Excel,Communication,Customer Success,Business Strategy. Job type: Internship.	Junior	Internship	Ho Chi Minh City	2026-03-19 15:16:50.235386	20500000.00	Operations Coordinator Junior #132	118	open	16500000	24500000	VND	2026-04-29 15:16:50.235386	\N	OPERATIONS	https://placehold.co/640x360/EFF6FF/2563EB?text=TTJobs
120	2026-06-14 15:16:50.235386	Demo seed: Help customers resolve issues with clear and friendly support. Key skills: Customer Support,Communication,CRM,Customer Success. Job type: Internship.	Junior	Internship	Ho Chi Minh City	2026-04-03 15:16:50.235386	13000000.00	Customer Support Specialist Junior #162	118	open	9000000	17000000	VND	2026-04-29 15:16:50.235386	\N	CUSTOMER-SERVICE	https://placehold.co/640x360/EFF6FF/2563EB?text=TTJobs
121	2026-05-30 15:16:50.235386	Demo seed: Coordinate daily operations and improve team execution. Key skills: Excel,Communication,Customer Success,Business Strategy. Job type: Internship.	Junior	Internship	Ho Chi Minh City	2026-04-18 15:16:50.235386	16000000.00	Operations Coordinator Junior #192	118	open	12000000	20000000	VND	2026-04-29 15:16:50.235386	\N	OPERATIONS	https://placehold.co/640x360/EFF6FF/2563EB?text=TTJobs
122	2026-06-29 15:16:50.235386	Demo seed: Help customers resolve issues with clear and friendly support. Key skills: Customer Support,Communication,CRM,Customer Success. Job type: Internship.	Junior	Internship	Ho Chi Minh City	2026-03-19 15:16:50.235386	19000000.00	Customer Support Specialist Junior #222	118	open	15000000	23000000	VND	2026-04-29 15:16:50.235386	\N	CUSTOMER-SERVICE	https://placehold.co/640x360/EFF6FF/2563EB?text=TTJobs
123	2026-06-14 15:16:50.235386	Demo seed: Coordinate daily operations and improve team execution. Key skills: Excel,Communication,Customer Success,Business Strategy. Job type: Internship.	Junior	Internship	Ho Chi Minh City	2026-04-03 15:16:50.235386	22000000.00	Operations Coordinator Junior #252	118	open	18000000	26000000	VND	2026-04-29 15:16:50.235386	\N	OPERATIONS	https://placehold.co/640x360/EFF6FF/2563EB?text=TTJobs
124	2026-05-30 15:16:50.235386	Demo seed: Help customers resolve issues with clear and friendly support. Key skills: Customer Support,Communication,CRM,Customer Success. Job type: Internship.	Junior	Internship	Ho Chi Minh City	2026-04-18 15:16:50.235386	14500000.00	Customer Support Specialist Junior #282	118	open	10500000	18500000	VND	2026-04-29 15:16:50.235386	\N	CUSTOMER-SERVICE	https://placehold.co/640x360/EFF6FF/2563EB?text=TTJobs
125	2026-05-31 15:16:50.235386	Demo seed: Build and maintain production software for growing product teams. Key skills: Java,Spring Boot,React,SQL,Docker. Job type: Full-time.	Mid	Full-time	Da Nang	2026-04-17 15:16:50.235386	24500000.00	Software Engineer Mid #013	119	open	20500000	28500000	VND	2026-04-29 15:16:50.235386	\N	INFORMATION-TECHNOLOGY	https://placehold.co/640x360/EFF6FF/2563EB?text=TTJobs
126	2026-06-30 15:16:50.235386	Demo seed: Advise customers and manage real estate opportunities. Key skills: Real Estate Sales,Property Management,Negotiation,CRM. Job type: Full-time.	Mid	Full-time	Da Nang	2026-03-18 15:16:50.235386	17000000.00	Property Consultant Mid #043	119	open	13000000	21000000	VND	2026-04-29 15:16:50.235386	\N	REAL-ESTATE	https://placehold.co/640x360/EFF6FF/2563EB?text=TTJobs
127	2026-06-15 15:16:50.235386	Demo seed: Build and maintain production software for growing product teams. Key skills: Java,Spring Boot,React,SQL,Docker. Job type: Full-time.	Mid	Full-time	Da Nang	2026-04-02 15:16:50.235386	20000000.00	Software Engineer Mid #073	119	open	16000000	24000000	VND	2026-04-29 15:16:50.235386	\N	INFORMATION-TECHNOLOGY	https://placehold.co/640x360/EFF6FF/2563EB?text=TTJobs
128	2026-05-31 15:16:50.235386	Demo seed: Advise customers and manage real estate opportunities. Key skills: Real Estate Sales,Property Management,Negotiation,CRM. Job type: Full-time.	Mid	Full-time	Da Nang	2026-04-17 15:16:50.235386	23000000.00	Property Consultant Mid #103	119	open	19000000	27000000	VND	2026-04-29 15:16:50.235386	\N	REAL-ESTATE	https://placehold.co/640x360/EFF6FF/2563EB?text=TTJobs
129	2026-06-30 15:16:50.235386	Demo seed: Build and maintain production software for growing product teams. Key skills: Java,Spring Boot,React,SQL,Docker. Job type: Full-time.	Mid	Full-time	Da Nang	2026-03-18 15:16:50.235386	26000000.00	Software Engineer Mid #133	119	open	22000000	30000000	VND	2026-04-29 15:16:50.235386	\N	INFORMATION-TECHNOLOGY	https://placehold.co/640x360/EFF6FF/2563EB?text=TTJobs
130	2026-06-15 15:16:50.235386	Demo seed: Advise customers and manage real estate opportunities. Key skills: Real Estate Sales,Property Management,Negotiation,CRM. Job type: Full-time.	Mid	Full-time	Da Nang	2026-04-02 15:16:50.235386	18500000.00	Property Consultant Mid #163	119	open	14500000	22500000	VND	2026-04-29 15:16:50.235386	\N	REAL-ESTATE	https://placehold.co/640x360/EFF6FF/2563EB?text=TTJobs
131	2026-05-31 15:16:50.235386	Demo seed: Build and maintain production software for growing product teams. Key skills: Java,Spring Boot,React,SQL,Docker. Job type: Full-time.	Mid	Full-time	Da Nang	2026-04-17 15:16:50.235386	21500000.00	Software Engineer Mid #193	119	open	17500000	25500000	VND	2026-04-29 15:16:50.235386	\N	INFORMATION-TECHNOLOGY	https://placehold.co/640x360/EFF6FF/2563EB?text=TTJobs
132	2026-06-30 15:16:50.235386	Demo seed: Advise customers and manage real estate opportunities. Key skills: Real Estate Sales,Property Management,Negotiation,CRM. Job type: Full-time.	Mid	Full-time	Da Nang	2026-03-18 15:16:50.235386	24500000.00	Property Consultant Mid #223	119	open	20500000	28500000	VND	2026-04-29 15:16:50.235386	\N	REAL-ESTATE	https://placehold.co/640x360/EFF6FF/2563EB?text=TTJobs
133	2026-06-15 15:16:50.235386	Demo seed: Build and maintain production software for growing product teams. Key skills: Java,Spring Boot,React,SQL,Docker. Job type: Full-time.	Mid	Full-time	Da Nang	2026-04-02 15:16:50.235386	17000000.00	Software Engineer Mid #253	119	open	13000000	21000000	VND	2026-04-29 15:16:50.235386	\N	INFORMATION-TECHNOLOGY	https://placehold.co/640x360/EFF6FF/2563EB?text=TTJobs
134	2026-05-31 15:16:50.235386	Demo seed: Advise customers and manage real estate opportunities. Key skills: Real Estate Sales,Property Management,Negotiation,CRM. Job type: Full-time.	Mid	Full-time	Da Nang	2026-04-17 15:16:50.235386	20000000.00	Property Consultant Mid #283	119	open	16000000	24000000	VND	2026-04-29 15:16:50.235386	\N	REAL-ESTATE	https://placehold.co/640x360/EFF6FF/2563EB?text=TTJobs
135	2026-06-01 15:16:50.235386	Demo seed: Own sales pipeline and close new business opportunities. Key skills: Sales,CRM,Negotiation,Customer Success. Job type: Part-time.	Senior	Part-time	Remote	2026-04-16 15:16:50.235386	32000000.00	Sales Executive Senior #014	120	open	28000000	36000000	VND	2026-04-29 15:16:50.235386	\N	SALES	https://placehold.co/640x360/EFF6FF/2563EB?text=TTJobs
136	2026-07-01 15:16:50.235386	Demo seed: Handle accounting records, reports and compliance tasks. Key skills: Accounting,Tax,Audit,Excel. Job type: Part-time.	Senior	Part-time	Remote	2026-03-17 15:16:50.235386	24500000.00	Accountant Senior #044	120	open	20500000	28500000	VND	2026-04-29 15:16:50.235386	\N	ACCOUNTING	https://placehold.co/640x360/EFF6FF/2563EB?text=TTJobs
137	2026-06-16 15:16:50.235386	Demo seed: Own sales pipeline and close new business opportunities. Key skills: Sales,CRM,Negotiation,Customer Success. Job type: Part-time.	Senior	Part-time	Remote	2026-04-01 15:16:50.235386	27500000.00	Sales Executive Senior #074	120	open	23500000	31500000	VND	2026-04-29 15:16:50.235386	\N	SALES	https://placehold.co/640x360/EFF6FF/2563EB?text=TTJobs
138	2026-06-01 15:16:50.235386	Demo seed: Handle accounting records, reports and compliance tasks. Key skills: Accounting,Tax,Audit,Excel. Job type: Part-time.	Senior	Part-time	Remote	2026-04-16 15:16:50.235386	30500000.00	Accountant Senior #104	120	open	26500000	34500000	VND	2026-04-29 15:16:50.235386	\N	ACCOUNTING	https://placehold.co/640x360/EFF6FF/2563EB?text=TTJobs
139	2026-07-01 15:16:50.235386	Demo seed: Own sales pipeline and close new business opportunities. Key skills: Sales,CRM,Negotiation,Customer Success. Job type: Part-time.	Senior	Part-time	Remote	2026-03-17 15:16:50.235386	23000000.00	Sales Executive Senior #134	120	open	19000000	27000000	VND	2026-04-29 15:16:50.235386	\N	SALES	https://placehold.co/640x360/EFF6FF/2563EB?text=TTJobs
140	2026-06-16 15:16:50.235386	Demo seed: Handle accounting records, reports and compliance tasks. Key skills: Accounting,Tax,Audit,Excel. Job type: Part-time.	Senior	Part-time	Remote	2026-04-01 15:16:50.235386	26000000.00	Accountant Senior #164	120	open	22000000	30000000	VND	2026-04-29 15:16:50.235386	\N	ACCOUNTING	https://placehold.co/640x360/EFF6FF/2563EB?text=TTJobs
141	2026-06-01 15:16:50.235386	Demo seed: Own sales pipeline and close new business opportunities. Key skills: Sales,CRM,Negotiation,Customer Success. Job type: Part-time.	Senior	Part-time	Remote	2026-04-16 15:16:50.235386	29000000.00	Sales Executive Senior #194	120	open	25000000	33000000	VND	2026-04-29 15:16:50.235386	\N	SALES	https://placehold.co/640x360/EFF6FF/2563EB?text=TTJobs
142	2026-07-01 15:16:50.235386	Demo seed: Handle accounting records, reports and compliance tasks. Key skills: Accounting,Tax,Audit,Excel. Job type: Part-time.	Senior	Part-time	Remote	2026-03-17 15:16:50.235386	32000000.00	Accountant Senior #224	120	open	28000000	36000000	VND	2026-04-29 15:16:50.235386	\N	ACCOUNTING	https://placehold.co/640x360/EFF6FF/2563EB?text=TTJobs
143	2026-06-16 15:16:50.235386	Demo seed: Own sales pipeline and close new business opportunities. Key skills: Sales,CRM,Negotiation,Customer Success. Job type: Part-time.	Senior	Part-time	Remote	2026-04-01 15:16:50.235386	24500000.00	Sales Executive Senior #254	120	open	20500000	28500000	VND	2026-04-29 15:16:50.235386	\N	SALES	https://placehold.co/640x360/EFF6FF/2563EB?text=TTJobs
144	2026-06-01 15:16:50.235386	Demo seed: Handle accounting records, reports and compliance tasks. Key skills: Accounting,Tax,Audit,Excel. Job type: Part-time.	Senior	Part-time	Remote	2026-04-16 15:16:50.235386	27500000.00	Accountant Senior #284	120	open	23500000	31500000	VND	2026-04-29 15:16:50.235386	\N	ACCOUNTING	https://placehold.co/640x360/EFF6FF/2563EB?text=TTJobs
145	2026-06-02 15:16:50.235386	Demo seed: Plan and execute digital campaigns across key channels. Key skills: SEO,Content Marketing,Google Ads,Social Media. Job type: Remote.	Lead	Remote	Hybrid	2026-04-15 15:16:50.235386	29000000.00	Digital Marketing Specialist Lead #015	121	open	25000000	33000000	VND	2026-04-29 15:16:50.235386	\N	MARKETING	https://placehold.co/640x360/EFF6FF/2563EB?text=TTJobs
146	2026-07-02 15:16:50.235386	Demo seed: Design thoughtful user experiences for web and mobile products. Key skills: Figma,UI Design,UX Research,Communication. Job type: Remote.	Lead	Remote	Hybrid	2026-03-16 15:16:50.235386	32000000.00	Product Designer Lead #045	121	open	28000000	36000000	VND	2026-04-29 15:16:50.235386	\N	DESIGN	https://placehold.co/640x360/EFF6FF/2563EB?text=TTJobs
147	2026-06-17 15:16:50.235386	Demo seed: Plan and execute digital campaigns across key channels. Key skills: SEO,Content Marketing,Google Ads,Social Media. Job type: Remote.	Lead	Remote	Hybrid	2026-03-31 15:16:50.235386	35000000.00	Digital Marketing Specialist Lead #075	121	open	31000000	39000000	VND	2026-04-29 15:16:50.235386	\N	MARKETING	https://placehold.co/640x360/EFF6FF/2563EB?text=TTJobs
148	2026-06-02 15:16:50.235386	Demo seed: Design thoughtful user experiences for web and mobile products. Key skills: Figma,UI Design,UX Research,Communication. Job type: Remote.	Lead	Remote	Hybrid	2026-04-15 15:16:50.235386	38000000.00	Product Designer Lead #105	121	open	34000000	42000000	VND	2026-04-29 15:16:50.235386	\N	DESIGN	https://placehold.co/640x360/EFF6FF/2563EB?text=TTJobs
149	2026-07-02 15:16:50.235386	Demo seed: Plan and execute digital campaigns across key channels. Key skills: SEO,Content Marketing,Google Ads,Social Media. Job type: Remote.	Lead	Remote	Hybrid	2026-03-16 15:16:50.235386	30500000.00	Digital Marketing Specialist Lead #135	121	open	26500000	34500000	VND	2026-04-29 15:16:50.235386	\N	MARKETING	https://placehold.co/640x360/EFF6FF/2563EB?text=TTJobs
150	2026-06-17 15:16:50.235386	Demo seed: Design thoughtful user experiences for web and mobile products. Key skills: Figma,UI Design,UX Research,Communication. Job type: Remote.	Lead	Remote	Hybrid	2026-03-31 15:16:50.235386	33500000.00	Product Designer Lead #165	121	open	29500000	37500000	VND	2026-04-29 15:16:50.235386	\N	DESIGN	https://placehold.co/640x360/EFF6FF/2563EB?text=TTJobs
151	2026-06-02 15:16:50.235386	Demo seed: Plan and execute digital campaigns across key channels. Key skills: SEO,Content Marketing,Google Ads,Social Media. Job type: Remote.	Lead	Remote	Hybrid	2026-04-15 15:16:50.235386	36500000.00	Digital Marketing Specialist Lead #195	121	open	32500000	40500000	VND	2026-04-29 15:16:50.235386	\N	MARKETING	https://placehold.co/640x360/EFF6FF/2563EB?text=TTJobs
152	2026-07-02 15:16:50.235386	Demo seed: Design thoughtful user experiences for web and mobile products. Key skills: Figma,UI Design,UX Research,Communication. Job type: Remote.	Lead	Remote	Hybrid	2026-03-16 15:16:50.235386	29000000.00	Product Designer Lead #225	121	open	25000000	33000000	VND	2026-04-29 15:16:50.235386	\N	DESIGN	https://placehold.co/640x360/EFF6FF/2563EB?text=TTJobs
153	2026-06-17 15:16:50.235386	Demo seed: Plan and execute digital campaigns across key channels. Key skills: SEO,Content Marketing,Google Ads,Social Media. Job type: Remote.	Lead	Remote	Hybrid	2026-03-31 15:16:50.235386	32000000.00	Digital Marketing Specialist Lead #255	121	open	28000000	36000000	VND	2026-04-29 15:16:50.235386	\N	MARKETING	https://placehold.co/640x360/EFF6FF/2563EB?text=TTJobs
154	2026-06-02 15:16:50.235386	Demo seed: Design thoughtful user experiences for web and mobile products. Key skills: Figma,UI Design,UX Research,Communication. Job type: Remote.	Lead	Remote	Hybrid	2026-04-15 15:16:50.235386	35000000.00	Product Designer Lead #285	121	open	31000000	39000000	VND	2026-04-29 15:16:50.235386	\N	DESIGN	https://placehold.co/640x360/EFF6FF/2563EB?text=TTJobs
155	2026-06-03 15:16:50.235386	Demo seed: Support hiring, onboarding and people operations. Key skills: Recruitment,HR Operations,Payroll,Communication. Job type: Hybrid.	Entry	Hybrid	Ha Noi	2026-04-14 15:16:50.235386	12500000.00	HR Operations Specialist Entry #016	122	open	8500000	16500000	VND	2026-04-29 15:16:50.235386	\N	HR	https://placehold.co/640x360/EFF6FF/2563EB?text=TTJobs
156	2026-05-19 15:16:50.235386	Demo seed: Develop partnerships and new growth opportunities. Key skills: Partnerships,Market Research,Business Strategy,Negotiation. Job type: Hybrid.	Entry	Hybrid	Ha Noi	2026-04-29 15:16:50.235386	15500000.00	Business Development Executive Entry #046	122	open	11500000	19500000	VND	2026-04-29 15:16:50.235386	\N	BUSINESS-DEVELOPMENT	https://placehold.co/640x360/EFF6FF/2563EB?text=TTJobs
157	2026-06-18 15:16:50.235386	Demo seed: Support hiring, onboarding and people operations. Key skills: Recruitment,HR Operations,Payroll,Communication. Job type: Hybrid.	Entry	Hybrid	Ha Noi	2026-03-30 15:16:50.235386	18500000.00	HR Operations Specialist Entry #076	122	open	14500000	22500000	VND	2026-04-29 15:16:50.235386	\N	HR	https://placehold.co/640x360/EFF6FF/2563EB?text=TTJobs
158	2026-06-03 15:16:50.235386	Demo seed: Develop partnerships and new growth opportunities. Key skills: Partnerships,Market Research,Business Strategy,Negotiation. Job type: Hybrid.	Entry	Hybrid	Ha Noi	2026-04-14 15:16:50.235386	11000000.00	Business Development Executive Entry #106	122	open	7000000	15000000	VND	2026-04-29 15:16:50.235386	\N	BUSINESS-DEVELOPMENT	https://placehold.co/640x360/EFF6FF/2563EB?text=TTJobs
159	2026-05-19 15:16:50.235386	Demo seed: Support hiring, onboarding and people operations. Key skills: Recruitment,HR Operations,Payroll,Communication. Job type: Hybrid.	Entry	Hybrid	Ha Noi	2026-04-29 15:16:50.235386	14000000.00	HR Operations Specialist Entry #136	122	open	10000000	18000000	VND	2026-04-29 15:16:50.235386	\N	HR	https://placehold.co/640x360/EFF6FF/2563EB?text=TTJobs
160	2026-06-18 15:16:50.235386	Demo seed: Develop partnerships and new growth opportunities. Key skills: Partnerships,Market Research,Business Strategy,Negotiation. Job type: Hybrid.	Entry	Hybrid	Ha Noi	2026-03-30 15:16:50.235386	17000000.00	Business Development Executive Entry #166	122	open	13000000	21000000	VND	2026-04-29 15:16:50.235386	\N	BUSINESS-DEVELOPMENT	https://placehold.co/640x360/EFF6FF/2563EB?text=TTJobs
161	2026-06-03 15:16:50.235386	Demo seed: Support hiring, onboarding and people operations. Key skills: Recruitment,HR Operations,Payroll,Communication. Job type: Hybrid.	Entry	Hybrid	Ha Noi	2026-04-14 15:16:50.235386	20000000.00	HR Operations Specialist Entry #196	122	open	16000000	24000000	VND	2026-04-29 15:16:50.235386	\N	HR	https://placehold.co/640x360/EFF6FF/2563EB?text=TTJobs
162	2026-05-19 15:16:50.235386	Demo seed: Develop partnerships and new growth opportunities. Key skills: Partnerships,Market Research,Business Strategy,Negotiation. Job type: Hybrid.	Entry	Hybrid	Ha Noi	2026-04-29 15:16:50.235386	12500000.00	Business Development Executive Entry #226	122	open	8500000	16500000	VND	2026-04-29 15:16:50.235386	\N	BUSINESS-DEVELOPMENT	https://placehold.co/640x360/EFF6FF/2563EB?text=TTJobs
163	2026-06-18 15:16:50.235386	Demo seed: Support hiring, onboarding and people operations. Key skills: Recruitment,HR Operations,Payroll,Communication. Job type: Hybrid.	Entry	Hybrid	Ha Noi	2026-03-30 15:16:50.235386	15500000.00	HR Operations Specialist Entry #256	122	open	11500000	19500000	VND	2026-04-29 15:16:50.235386	\N	HR	https://placehold.co/640x360/EFF6FF/2563EB?text=TTJobs
164	2026-06-03 15:16:50.235386	Demo seed: Develop partnerships and new growth opportunities. Key skills: Partnerships,Market Research,Business Strategy,Negotiation. Job type: Hybrid.	Entry	Hybrid	Ha Noi	2026-04-14 15:16:50.235386	18500000.00	Business Development Executive Entry #286	122	open	14500000	22500000	VND	2026-04-29 15:16:50.235386	\N	BUSINESS-DEVELOPMENT	https://placehold.co/640x360/EFF6FF/2563EB?text=TTJobs
165	2026-06-04 15:16:50.235386	Demo seed: Analyze financial performance and support planning decisions. Key skills: Financial Analysis,Risk Management,Excel,SQL. Job type: Contract.	Junior	Contract	Ho Chi Minh City	2026-04-13 15:16:50.235386	16000000.00	Financial Analyst Junior #017	123	open	12000000	20000000	VND	2026-04-29 15:16:50.235386	\N	FINANCE	https://placehold.co/640x360/EFF6FF/2563EB?text=TTJobs
166	2026-05-20 15:16:50.235386	Demo seed: Design practical technical solutions for customer and internal needs. Key skills: Python,AWS,Docker,SQL. Job type: Contract.	Junior	Contract	Ho Chi Minh City	2026-04-28 15:16:50.235386	19000000.00	Solutions Engineer Junior #047	123	open	15000000	23000000	VND	2026-04-29 15:16:50.235386	\N	ENGINEERING	https://placehold.co/640x360/EFF6FF/2563EB?text=TTJobs
167	2026-06-19 15:16:50.235386	Demo seed: Analyze financial performance and support planning decisions. Key skills: Financial Analysis,Risk Management,Excel,SQL. Job type: Contract.	Junior	Contract	Ho Chi Minh City	2026-03-29 15:16:50.235386	22000000.00	Financial Analyst Junior #077	123	open	18000000	26000000	VND	2026-04-29 15:16:50.235386	\N	FINANCE	https://placehold.co/640x360/EFF6FF/2563EB?text=TTJobs
168	2026-06-04 15:16:50.235386	Demo seed: Design practical technical solutions for customer and internal needs. Key skills: Python,AWS,Docker,SQL. Job type: Contract.	Junior	Contract	Ho Chi Minh City	2026-04-13 15:16:50.235386	14500000.00	Solutions Engineer Junior #107	123	open	10500000	18500000	VND	2026-04-29 15:16:50.235386	\N	ENGINEERING	https://placehold.co/640x360/EFF6FF/2563EB?text=TTJobs
169	2026-05-20 15:16:50.235386	Demo seed: Analyze financial performance and support planning decisions. Key skills: Financial Analysis,Risk Management,Excel,SQL. Job type: Contract.	Junior	Contract	Ho Chi Minh City	2026-04-28 15:16:50.235386	17500000.00	Financial Analyst Junior #137	123	open	13500000	21500000	VND	2026-04-29 15:16:50.235386	\N	FINANCE	https://placehold.co/640x360/EFF6FF/2563EB?text=TTJobs
170	2026-06-19 15:16:50.235386	Demo seed: Design practical technical solutions for customer and internal needs. Key skills: Python,AWS,Docker,SQL. Job type: Contract.	Junior	Contract	Ho Chi Minh City	2026-03-29 15:16:50.235386	20500000.00	Solutions Engineer Junior #167	123	open	16500000	24500000	VND	2026-04-29 15:16:50.235386	\N	ENGINEERING	https://placehold.co/640x360/EFF6FF/2563EB?text=TTJobs
171	2026-06-04 15:16:50.235386	Demo seed: Analyze financial performance and support planning decisions. Key skills: Financial Analysis,Risk Management,Excel,SQL. Job type: Contract.	Junior	Contract	Ho Chi Minh City	2026-04-13 15:16:50.235386	13000000.00	Financial Analyst Junior #197	123	open	9000000	17000000	VND	2026-04-29 15:16:50.235386	\N	FINANCE	https://placehold.co/640x360/EFF6FF/2563EB?text=TTJobs
172	2026-05-20 15:16:50.235386	Demo seed: Design practical technical solutions for customer and internal needs. Key skills: Python,AWS,Docker,SQL. Job type: Contract.	Junior	Contract	Ho Chi Minh City	2026-04-28 15:16:50.235386	16000000.00	Solutions Engineer Junior #227	123	open	12000000	20000000	VND	2026-04-29 15:16:50.235386	\N	ENGINEERING	https://placehold.co/640x360/EFF6FF/2563EB?text=TTJobs
173	2026-06-19 15:16:50.235386	Demo seed: Analyze financial performance and support planning decisions. Key skills: Financial Analysis,Risk Management,Excel,SQL. Job type: Contract.	Junior	Contract	Ho Chi Minh City	2026-03-29 15:16:50.235386	19000000.00	Financial Analyst Junior #257	123	open	15000000	23000000	VND	2026-04-29 15:16:50.235386	\N	FINANCE	https://placehold.co/640x360/EFF6FF/2563EB?text=TTJobs
174	2026-06-04 15:16:50.235386	Demo seed: Design practical technical solutions for customer and internal needs. Key skills: Python,AWS,Docker,SQL. Job type: Contract.	Junior	Contract	Ho Chi Minh City	2026-04-13 15:16:50.235386	22000000.00	Solutions Engineer Junior #287	123	open	18000000	26000000	VND	2026-04-29 15:16:50.235386	\N	ENGINEERING	https://placehold.co/640x360/EFF6FF/2563EB?text=TTJobs
175	2026-06-05 15:16:50.235386	Demo seed: Help customers resolve issues with clear and friendly support. Key skills: Customer Support,Communication,CRM,Customer Success. Job type: Internship.	Mid	Internship	Da Nang	2026-04-12 15:16:50.235386	21500000.00	Customer Support Specialist Mid #018	124	open	17500000	25500000	VND	2026-04-29 15:16:50.235386	\N	CUSTOMER-SERVICE	https://placehold.co/640x360/EFF6FF/2563EB?text=TTJobs
176	2026-05-21 15:16:50.235386	Demo seed: Coordinate daily operations and improve team execution. Key skills: Excel,Communication,Customer Success,Business Strategy. Job type: Internship.	Mid	Internship	Da Nang	2026-04-27 15:16:50.235386	24500000.00	Operations Coordinator Mid #048	124	open	20500000	28500000	VND	2026-04-29 15:16:50.235386	\N	OPERATIONS	https://placehold.co/640x360/EFF6FF/2563EB?text=TTJobs
177	2026-06-20 15:16:50.235386	Demo seed: Help customers resolve issues with clear and friendly support. Key skills: Customer Support,Communication,CRM,Customer Success. Job type: Internship.	Mid	Internship	Da Nang	2026-03-28 15:16:50.235386	17000000.00	Customer Support Specialist Mid #078	124	open	13000000	21000000	VND	2026-04-29 15:16:50.235386	\N	CUSTOMER-SERVICE	https://placehold.co/640x360/EFF6FF/2563EB?text=TTJobs
178	2026-06-05 15:16:50.235386	Demo seed: Coordinate daily operations and improve team execution. Key skills: Excel,Communication,Customer Success,Business Strategy. Job type: Internship.	Mid	Internship	Da Nang	2026-04-12 15:16:50.235386	20000000.00	Operations Coordinator Mid #108	124	open	16000000	24000000	VND	2026-04-29 15:16:50.235386	\N	OPERATIONS	https://placehold.co/640x360/EFF6FF/2563EB?text=TTJobs
179	2026-05-21 15:16:50.235386	Demo seed: Help customers resolve issues with clear and friendly support. Key skills: Customer Support,Communication,CRM,Customer Success. Job type: Internship.	Mid	Internship	Da Nang	2026-04-27 15:16:50.235386	23000000.00	Customer Support Specialist Mid #138	124	open	19000000	27000000	VND	2026-04-29 15:16:50.235386	\N	CUSTOMER-SERVICE	https://placehold.co/640x360/EFF6FF/2563EB?text=TTJobs
180	2026-06-20 15:16:50.235386	Demo seed: Coordinate daily operations and improve team execution. Key skills: Excel,Communication,Customer Success,Business Strategy. Job type: Internship.	Mid	Internship	Da Nang	2026-03-28 15:16:50.235386	26000000.00	Operations Coordinator Mid #168	124	open	22000000	30000000	VND	2026-04-29 15:16:50.235386	\N	OPERATIONS	https://placehold.co/640x360/EFF6FF/2563EB?text=TTJobs
181	2026-06-05 15:16:50.235386	Demo seed: Help customers resolve issues with clear and friendly support. Key skills: Customer Support,Communication,CRM,Customer Success. Job type: Internship.	Mid	Internship	Da Nang	2026-04-12 15:16:50.235386	18500000.00	Customer Support Specialist Mid #198	124	open	14500000	22500000	VND	2026-04-29 15:16:50.235386	\N	CUSTOMER-SERVICE	https://placehold.co/640x360/EFF6FF/2563EB?text=TTJobs
182	2026-05-21 15:16:50.235386	Demo seed: Coordinate daily operations and improve team execution. Key skills: Excel,Communication,Customer Success,Business Strategy. Job type: Internship.	Mid	Internship	Da Nang	2026-04-27 15:16:50.235386	21500000.00	Operations Coordinator Mid #228	124	open	17500000	25500000	VND	2026-04-29 15:16:50.235386	\N	OPERATIONS	https://placehold.co/640x360/EFF6FF/2563EB?text=TTJobs
183	2026-06-20 15:16:50.235386	Demo seed: Help customers resolve issues with clear and friendly support. Key skills: Customer Support,Communication,CRM,Customer Success. Job type: Internship.	Mid	Internship	Da Nang	2026-03-28 15:16:50.235386	24500000.00	Customer Support Specialist Mid #258	124	open	20500000	28500000	VND	2026-04-29 15:16:50.235386	\N	CUSTOMER-SERVICE	https://placehold.co/640x360/EFF6FF/2563EB?text=TTJobs
184	2026-06-05 15:16:50.235386	Demo seed: Coordinate daily operations and improve team execution. Key skills: Excel,Communication,Customer Success,Business Strategy. Job type: Internship.	Mid	Internship	Da Nang	2026-04-12 15:16:50.235386	17000000.00	Operations Coordinator Mid #288	124	open	13000000	21000000	VND	2026-04-29 15:16:50.235386	\N	OPERATIONS	https://placehold.co/640x360/EFF6FF/2563EB?text=TTJobs
185	2026-06-06 15:16:50.235386	Demo seed: Advise customers and manage real estate opportunities. Key skills: Real Estate Sales,Property Management,Negotiation,CRM. Job type: Full-time.	Senior	Full-time	Remote	2026-04-11 15:16:50.235386	29000000.00	Property Consultant Senior #019	125	open	25000000	33000000	VND	2026-04-29 15:16:50.235386	\N	REAL-ESTATE	https://placehold.co/640x360/EFF6FF/2563EB?text=TTJobs
186	2026-05-22 15:16:50.235386	Demo seed: Build and maintain production software for growing product teams. Key skills: Java,Spring Boot,React,SQL,Docker. Job type: Full-time.	Senior	Full-time	Remote	2026-04-26 15:16:50.235386	32000000.00	Software Engineer Senior #049	125	open	28000000	36000000	VND	2026-04-29 15:16:50.235386	\N	INFORMATION-TECHNOLOGY	https://placehold.co/640x360/EFF6FF/2563EB?text=TTJobs
187	2026-06-21 15:16:50.235386	Demo seed: Advise customers and manage real estate opportunities. Key skills: Real Estate Sales,Property Management,Negotiation,CRM. Job type: Full-time.	Senior	Full-time	Remote	2026-03-27 15:16:50.235386	24500000.00	Property Consultant Senior #079	125	open	20500000	28500000	VND	2026-04-29 15:16:50.235386	\N	REAL-ESTATE	https://placehold.co/640x360/EFF6FF/2563EB?text=TTJobs
188	2026-06-06 15:16:50.235386	Demo seed: Build and maintain production software for growing product teams. Key skills: Java,Spring Boot,React,SQL,Docker. Job type: Full-time.	Senior	Full-time	Remote	2026-04-11 15:16:50.235386	27500000.00	Software Engineer Senior #109	125	open	23500000	31500000	VND	2026-04-29 15:16:50.235386	\N	INFORMATION-TECHNOLOGY	https://placehold.co/640x360/EFF6FF/2563EB?text=TTJobs
189	2026-05-22 15:16:50.235386	Demo seed: Advise customers and manage real estate opportunities. Key skills: Real Estate Sales,Property Management,Negotiation,CRM. Job type: Full-time.	Senior	Full-time	Remote	2026-04-26 15:16:50.235386	30500000.00	Property Consultant Senior #139	125	open	26500000	34500000	VND	2026-04-29 15:16:50.235386	\N	REAL-ESTATE	https://placehold.co/640x360/EFF6FF/2563EB?text=TTJobs
190	2026-06-21 15:16:50.235386	Demo seed: Build and maintain production software for growing product teams. Key skills: Java,Spring Boot,React,SQL,Docker. Job type: Full-time.	Senior	Full-time	Remote	2026-03-27 15:16:50.235386	23000000.00	Software Engineer Senior #169	125	open	19000000	27000000	VND	2026-04-29 15:16:50.235386	\N	INFORMATION-TECHNOLOGY	https://placehold.co/640x360/EFF6FF/2563EB?text=TTJobs
191	2026-06-06 15:16:50.235386	Demo seed: Advise customers and manage real estate opportunities. Key skills: Real Estate Sales,Property Management,Negotiation,CRM. Job type: Full-time.	Senior	Full-time	Remote	2026-04-11 15:16:50.235386	26000000.00	Property Consultant Senior #199	125	open	22000000	30000000	VND	2026-04-29 15:16:50.235386	\N	REAL-ESTATE	https://placehold.co/640x360/EFF6FF/2563EB?text=TTJobs
192	2026-05-22 15:16:50.235386	Demo seed: Build and maintain production software for growing product teams. Key skills: Java,Spring Boot,React,SQL,Docker. Job type: Full-time.	Senior	Full-time	Remote	2026-04-26 15:16:50.235386	29000000.00	Software Engineer Senior #229	125	open	25000000	33000000	VND	2026-04-29 15:16:50.235386	\N	INFORMATION-TECHNOLOGY	https://placehold.co/640x360/EFF6FF/2563EB?text=TTJobs
193	2026-06-21 15:16:50.235386	Demo seed: Advise customers and manage real estate opportunities. Key skills: Real Estate Sales,Property Management,Negotiation,CRM. Job type: Full-time.	Senior	Full-time	Remote	2026-03-27 15:16:50.235386	32000000.00	Property Consultant Senior #259	125	open	28000000	36000000	VND	2026-04-29 15:16:50.235386	\N	REAL-ESTATE	https://placehold.co/640x360/EFF6FF/2563EB?text=TTJobs
194	2026-06-06 15:16:50.235386	Demo seed: Build and maintain production software for growing product teams. Key skills: Java,Spring Boot,React,SQL,Docker. Job type: Full-time.	Senior	Full-time	Remote	2026-04-11 15:16:50.235386	24500000.00	Software Engineer Senior #289	125	open	20500000	28500000	VND	2026-04-29 15:16:50.235386	\N	INFORMATION-TECHNOLOGY	https://placehold.co/640x360/EFF6FF/2563EB?text=TTJobs
195	2026-06-07 15:16:50.235386	Demo seed: Handle accounting records, reports and compliance tasks. Key skills: Accounting,Tax,Audit,Excel. Job type: Part-time.	Lead	Part-time	Hybrid	2026-04-10 15:16:50.235386	36500000.00	Accountant Lead #020	126	open	32500000	40500000	VND	2026-04-29 15:16:50.235386	\N	ACCOUNTING	https://placehold.co/640x360/EFF6FF/2563EB?text=TTJobs
196	2026-05-23 15:16:50.235386	Demo seed: Own sales pipeline and close new business opportunities. Key skills: Sales,CRM,Negotiation,Customer Success. Job type: Part-time.	Lead	Part-time	Hybrid	2026-04-25 15:16:50.235386	29000000.00	Sales Executive Lead #050	126	open	25000000	33000000	VND	2026-04-29 15:16:50.235386	\N	SALES	https://placehold.co/640x360/EFF6FF/2563EB?text=TTJobs
197	2026-06-22 15:16:50.235386	Demo seed: Handle accounting records, reports and compliance tasks. Key skills: Accounting,Tax,Audit,Excel. Job type: Part-time.	Lead	Part-time	Hybrid	2026-03-26 15:16:50.235386	32000000.00	Accountant Lead #080	126	open	28000000	36000000	VND	2026-04-29 15:16:50.235386	\N	ACCOUNTING	https://placehold.co/640x360/EFF6FF/2563EB?text=TTJobs
198	2026-06-07 15:16:50.235386	Demo seed: Own sales pipeline and close new business opportunities. Key skills: Sales,CRM,Negotiation,Customer Success. Job type: Part-time.	Lead	Part-time	Hybrid	2026-04-10 15:16:50.235386	35000000.00	Sales Executive Lead #110	126	open	31000000	39000000	VND	2026-04-29 15:16:50.235386	\N	SALES	https://placehold.co/640x360/EFF6FF/2563EB?text=TTJobs
199	2026-05-23 15:16:50.235386	Demo seed: Handle accounting records, reports and compliance tasks. Key skills: Accounting,Tax,Audit,Excel. Job type: Part-time.	Lead	Part-time	Hybrid	2026-04-25 15:16:50.235386	38000000.00	Accountant Lead #140	126	open	34000000	42000000	VND	2026-04-29 15:16:50.235386	\N	ACCOUNTING	https://placehold.co/640x360/EFF6FF/2563EB?text=TTJobs
200	2026-06-22 15:16:50.235386	Demo seed: Own sales pipeline and close new business opportunities. Key skills: Sales,CRM,Negotiation,Customer Success. Job type: Part-time.	Lead	Part-time	Hybrid	2026-03-26 15:16:50.235386	30500000.00	Sales Executive Lead #170	126	open	26500000	34500000	VND	2026-04-29 15:16:50.235386	\N	SALES	https://placehold.co/640x360/EFF6FF/2563EB?text=TTJobs
201	2026-06-07 15:16:50.235386	Demo seed: Handle accounting records, reports and compliance tasks. Key skills: Accounting,Tax,Audit,Excel. Job type: Part-time.	Lead	Part-time	Hybrid	2026-04-10 15:16:50.235386	33500000.00	Accountant Lead #200	126	open	29500000	37500000	VND	2026-04-29 15:16:50.235386	\N	ACCOUNTING	https://placehold.co/640x360/EFF6FF/2563EB?text=TTJobs
202	2026-05-23 15:16:50.235386	Demo seed: Own sales pipeline and close new business opportunities. Key skills: Sales,CRM,Negotiation,Customer Success. Job type: Part-time.	Lead	Part-time	Hybrid	2026-04-25 15:16:50.235386	36500000.00	Sales Executive Lead #230	126	open	32500000	40500000	VND	2026-04-29 15:16:50.235386	\N	SALES	https://placehold.co/640x360/EFF6FF/2563EB?text=TTJobs
203	2026-06-22 15:16:50.235386	Demo seed: Handle accounting records, reports and compliance tasks. Key skills: Accounting,Tax,Audit,Excel. Job type: Part-time.	Lead	Part-time	Hybrid	2026-03-26 15:16:50.235386	29000000.00	Accountant Lead #260	126	open	25000000	33000000	VND	2026-04-29 15:16:50.235386	\N	ACCOUNTING	https://placehold.co/640x360/EFF6FF/2563EB?text=TTJobs
204	2026-06-07 15:16:50.235386	Demo seed: Own sales pipeline and close new business opportunities. Key skills: Sales,CRM,Negotiation,Customer Success. Job type: Part-time.	Lead	Part-time	Hybrid	2026-04-10 15:16:50.235386	32000000.00	Sales Executive Lead #290	126	open	28000000	36000000	VND	2026-04-29 15:16:50.235386	\N	SALES	https://placehold.co/640x360/EFF6FF/2563EB?text=TTJobs
205	2026-06-08 15:16:50.235386	Demo seed: Design thoughtful user experiences for web and mobile products. Key skills: Figma,UI Design,UX Research,Communication. Job type: Remote.	Entry	Remote	Ha Noi	2026-04-09 15:16:50.235386	20000000.00	Product Designer Entry #021	127	open	16000000	24000000	VND	2026-04-29 15:16:50.235386	\N	DESIGN	https://placehold.co/640x360/EFF6FF/2563EB?text=TTJobs
206	2026-05-24 15:16:50.235386	Demo seed: Plan and execute digital campaigns across key channels. Key skills: SEO,Content Marketing,Google Ads,Social Media. Job type: Remote.	Entry	Remote	Ha Noi	2026-04-24 15:16:50.235386	12500000.00	Digital Marketing Specialist Entry #051	127	open	8500000	16500000	VND	2026-04-29 15:16:50.235386	\N	MARKETING	https://placehold.co/640x360/EFF6FF/2563EB?text=TTJobs
207	2026-06-23 15:16:50.235386	Demo seed: Design thoughtful user experiences for web and mobile products. Key skills: Figma,UI Design,UX Research,Communication. Job type: Remote.	Entry	Remote	Ha Noi	2026-03-25 15:16:50.235386	15500000.00	Product Designer Entry #081	127	open	11500000	19500000	VND	2026-04-29 15:16:50.235386	\N	DESIGN	https://placehold.co/640x360/EFF6FF/2563EB?text=TTJobs
208	2026-06-08 15:16:50.235386	Demo seed: Plan and execute digital campaigns across key channels. Key skills: SEO,Content Marketing,Google Ads,Social Media. Job type: Remote.	Entry	Remote	Ha Noi	2026-04-09 15:16:50.235386	18500000.00	Digital Marketing Specialist Entry #111	127	open	14500000	22500000	VND	2026-04-29 15:16:50.235386	\N	MARKETING	https://placehold.co/640x360/EFF6FF/2563EB?text=TTJobs
209	2026-05-24 15:16:50.235386	Demo seed: Design thoughtful user experiences for web and mobile products. Key skills: Figma,UI Design,UX Research,Communication. Job type: Remote.	Entry	Remote	Ha Noi	2026-04-24 15:16:50.235386	11000000.00	Product Designer Entry #141	127	open	7000000	15000000	VND	2026-04-29 15:16:50.235386	\N	DESIGN	https://placehold.co/640x360/EFF6FF/2563EB?text=TTJobs
210	2026-06-23 15:16:50.235386	Demo seed: Plan and execute digital campaigns across key channels. Key skills: SEO,Content Marketing,Google Ads,Social Media. Job type: Remote.	Entry	Remote	Ha Noi	2026-03-25 15:16:50.235386	14000000.00	Digital Marketing Specialist Entry #171	127	open	10000000	18000000	VND	2026-04-29 15:16:50.235386	\N	MARKETING	https://placehold.co/640x360/EFF6FF/2563EB?text=TTJobs
211	2026-06-08 15:16:50.235386	Demo seed: Design thoughtful user experiences for web and mobile products. Key skills: Figma,UI Design,UX Research,Communication. Job type: Remote.	Entry	Remote	Ha Noi	2026-04-09 15:16:50.235386	17000000.00	Product Designer Entry #201	127	open	13000000	21000000	VND	2026-04-29 15:16:50.235386	\N	DESIGN	https://placehold.co/640x360/EFF6FF/2563EB?text=TTJobs
212	2026-05-24 15:16:50.235386	Demo seed: Plan and execute digital campaigns across key channels. Key skills: SEO,Content Marketing,Google Ads,Social Media. Job type: Remote.	Entry	Remote	Ha Noi	2026-04-24 15:16:50.235386	20000000.00	Digital Marketing Specialist Entry #231	127	open	16000000	24000000	VND	2026-04-29 15:16:50.235386	\N	MARKETING	https://placehold.co/640x360/EFF6FF/2563EB?text=TTJobs
213	2026-06-23 15:16:50.235386	Demo seed: Design thoughtful user experiences for web and mobile products. Key skills: Figma,UI Design,UX Research,Communication. Job type: Remote.	Entry	Remote	Ha Noi	2026-03-25 15:16:50.235386	12500000.00	Product Designer Entry #261	127	open	8500000	16500000	VND	2026-04-29 15:16:50.235386	\N	DESIGN	https://placehold.co/640x360/EFF6FF/2563EB?text=TTJobs
214	2026-06-08 15:16:50.235386	Demo seed: Plan and execute digital campaigns across key channels. Key skills: SEO,Content Marketing,Google Ads,Social Media. Job type: Remote.	Entry	Remote	Ha Noi	2026-04-09 15:16:50.235386	15500000.00	Digital Marketing Specialist Entry #291	127	open	11500000	19500000	VND	2026-04-29 15:16:50.235386	\N	MARKETING	https://placehold.co/640x360/EFF6FF/2563EB?text=TTJobs
215	2026-06-09 15:16:50.235386	Demo seed: Develop partnerships and new growth opportunities. Key skills: Partnerships,Market Research,Business Strategy,Negotiation. Job type: Hybrid.	Junior	Hybrid	Ho Chi Minh City	2026-04-08 15:16:50.235386	13000000.00	Business Development Executive Junior #022	128	open	9000000	17000000	VND	2026-04-29 15:16:50.235386	\N	BUSINESS-DEVELOPMENT	https://placehold.co/640x360/EFF6FF/2563EB?text=TTJobs
216	2026-05-25 15:16:50.235386	Demo seed: Support hiring, onboarding and people operations. Key skills: Recruitment,HR Operations,Payroll,Communication. Job type: Hybrid.	Junior	Hybrid	Ho Chi Minh City	2026-04-23 15:16:50.235386	16000000.00	HR Operations Specialist Junior #052	128	open	12000000	20000000	VND	2026-04-29 15:16:50.235386	\N	HR	https://placehold.co/640x360/EFF6FF/2563EB?text=TTJobs
217	2026-06-24 15:16:50.235386	Demo seed: Develop partnerships and new growth opportunities. Key skills: Partnerships,Market Research,Business Strategy,Negotiation. Job type: Hybrid.	Junior	Hybrid	Ho Chi Minh City	2026-03-24 15:16:50.235386	19000000.00	Business Development Executive Junior #082	128	open	15000000	23000000	VND	2026-04-29 15:16:50.235386	\N	BUSINESS-DEVELOPMENT	https://placehold.co/640x360/EFF6FF/2563EB?text=TTJobs
218	2026-06-09 15:16:50.235386	Demo seed: Support hiring, onboarding and people operations. Key skills: Recruitment,HR Operations,Payroll,Communication. Job type: Hybrid.	Junior	Hybrid	Ho Chi Minh City	2026-04-08 15:16:50.235386	22000000.00	HR Operations Specialist Junior #112	128	open	18000000	26000000	VND	2026-04-29 15:16:50.235386	\N	HR	https://placehold.co/640x360/EFF6FF/2563EB?text=TTJobs
219	2026-05-25 15:16:50.235386	Demo seed: Develop partnerships and new growth opportunities. Key skills: Partnerships,Market Research,Business Strategy,Negotiation. Job type: Hybrid.	Junior	Hybrid	Ho Chi Minh City	2026-04-23 15:16:50.235386	14500000.00	Business Development Executive Junior #142	128	open	10500000	18500000	VND	2026-04-29 15:16:50.235386	\N	BUSINESS-DEVELOPMENT	https://placehold.co/640x360/EFF6FF/2563EB?text=TTJobs
220	2026-06-24 15:16:50.235386	Demo seed: Support hiring, onboarding and people operations. Key skills: Recruitment,HR Operations,Payroll,Communication. Job type: Hybrid.	Junior	Hybrid	Ho Chi Minh City	2026-03-24 15:16:50.235386	17500000.00	HR Operations Specialist Junior #172	128	open	13500000	21500000	VND	2026-04-29 15:16:50.235386	\N	HR	https://placehold.co/640x360/EFF6FF/2563EB?text=TTJobs
221	2026-06-09 15:16:50.235386	Demo seed: Develop partnerships and new growth opportunities. Key skills: Partnerships,Market Research,Business Strategy,Negotiation. Job type: Hybrid.	Junior	Hybrid	Ho Chi Minh City	2026-04-08 15:16:50.235386	20500000.00	Business Development Executive Junior #202	128	open	16500000	24500000	VND	2026-04-29 15:16:50.235386	\N	BUSINESS-DEVELOPMENT	https://placehold.co/640x360/EFF6FF/2563EB?text=TTJobs
222	2026-05-25 15:16:50.235386	Demo seed: Support hiring, onboarding and people operations. Key skills: Recruitment,HR Operations,Payroll,Communication. Job type: Hybrid.	Junior	Hybrid	Ho Chi Minh City	2026-04-23 15:16:50.235386	13000000.00	HR Operations Specialist Junior #232	128	open	9000000	17000000	VND	2026-04-29 15:16:50.235386	\N	HR	https://placehold.co/640x360/EFF6FF/2563EB?text=TTJobs
223	2026-06-24 15:16:50.235386	Demo seed: Develop partnerships and new growth opportunities. Key skills: Partnerships,Market Research,Business Strategy,Negotiation. Job type: Hybrid.	Junior	Hybrid	Ho Chi Minh City	2026-03-24 15:16:50.235386	16000000.00	Business Development Executive Junior #262	128	open	12000000	20000000	VND	2026-04-29 15:16:50.235386	\N	BUSINESS-DEVELOPMENT	https://placehold.co/640x360/EFF6FF/2563EB?text=TTJobs
224	2026-06-09 15:16:50.235386	Demo seed: Support hiring, onboarding and people operations. Key skills: Recruitment,HR Operations,Payroll,Communication. Job type: Hybrid.	Junior	Hybrid	Ho Chi Minh City	2026-04-08 15:16:50.235386	19000000.00	HR Operations Specialist Junior #292	128	open	15000000	23000000	VND	2026-04-29 15:16:50.235386	\N	HR	https://placehold.co/640x360/EFF6FF/2563EB?text=TTJobs
225	2026-06-10 15:16:50.235386	Demo seed: Design practical technical solutions for customer and internal needs. Key skills: Python,AWS,Docker,SQL. Job type: Contract.	Mid	Contract	Da Nang	2026-04-07 15:16:50.235386	18500000.00	Solutions Engineer Mid #023	129	open	14500000	22500000	VND	2026-04-29 15:16:50.235386	\N	ENGINEERING	https://placehold.co/640x360/EFF6FF/2563EB?text=TTJobs
226	2026-05-26 15:16:50.235386	Demo seed: Analyze financial performance and support planning decisions. Key skills: Financial Analysis,Risk Management,Excel,SQL. Job type: Contract.	Mid	Contract	Da Nang	2026-04-22 15:16:50.235386	21500000.00	Financial Analyst Mid #053	129	open	17500000	25500000	VND	2026-04-29 15:16:50.235386	\N	FINANCE	https://placehold.co/640x360/EFF6FF/2563EB?text=TTJobs
227	2026-06-25 15:16:50.235386	Demo seed: Design practical technical solutions for customer and internal needs. Key skills: Python,AWS,Docker,SQL. Job type: Contract.	Mid	Contract	Da Nang	2026-03-23 15:16:50.235386	24500000.00	Solutions Engineer Mid #083	129	open	20500000	28500000	VND	2026-04-29 15:16:50.235386	\N	ENGINEERING	https://placehold.co/640x360/EFF6FF/2563EB?text=TTJobs
228	2026-06-10 15:16:50.235386	Demo seed: Analyze financial performance and support planning decisions. Key skills: Financial Analysis,Risk Management,Excel,SQL. Job type: Contract.	Mid	Contract	Da Nang	2026-04-07 15:16:50.235386	17000000.00	Financial Analyst Mid #113	129	open	13000000	21000000	VND	2026-04-29 15:16:50.235386	\N	FINANCE	https://placehold.co/640x360/EFF6FF/2563EB?text=TTJobs
229	2026-05-26 15:16:50.235386	Demo seed: Design practical technical solutions for customer and internal needs. Key skills: Python,AWS,Docker,SQL. Job type: Contract.	Mid	Contract	Da Nang	2026-04-22 15:16:50.235386	20000000.00	Solutions Engineer Mid #143	129	open	16000000	24000000	VND	2026-04-29 15:16:50.235386	\N	ENGINEERING	https://placehold.co/640x360/EFF6FF/2563EB?text=TTJobs
230	2026-06-25 15:16:50.235386	Demo seed: Analyze financial performance and support planning decisions. Key skills: Financial Analysis,Risk Management,Excel,SQL. Job type: Contract.	Mid	Contract	Da Nang	2026-03-23 15:16:50.235386	23000000.00	Financial Analyst Mid #173	129	open	19000000	27000000	VND	2026-04-29 15:16:50.235386	\N	FINANCE	https://placehold.co/640x360/EFF6FF/2563EB?text=TTJobs
231	2026-06-10 15:16:50.235386	Demo seed: Design practical technical solutions for customer and internal needs. Key skills: Python,AWS,Docker,SQL. Job type: Contract.	Mid	Contract	Da Nang	2026-04-07 15:16:50.235386	26000000.00	Solutions Engineer Mid #203	129	open	22000000	30000000	VND	2026-04-29 15:16:50.235386	\N	ENGINEERING	https://placehold.co/640x360/EFF6FF/2563EB?text=TTJobs
232	2026-05-26 15:16:50.235386	Demo seed: Analyze financial performance and support planning decisions. Key skills: Financial Analysis,Risk Management,Excel,SQL. Job type: Contract.	Mid	Contract	Da Nang	2026-04-22 15:16:50.235386	18500000.00	Financial Analyst Mid #233	129	open	14500000	22500000	VND	2026-04-29 15:16:50.235386	\N	FINANCE	https://placehold.co/640x360/EFF6FF/2563EB?text=TTJobs
233	2026-06-25 15:16:50.235386	Demo seed: Design practical technical solutions for customer and internal needs. Key skills: Python,AWS,Docker,SQL. Job type: Contract.	Mid	Contract	Da Nang	2026-03-23 15:16:50.235386	21500000.00	Solutions Engineer Mid #263	129	open	17500000	25500000	VND	2026-04-29 15:16:50.235386	\N	ENGINEERING	https://placehold.co/640x360/EFF6FF/2563EB?text=TTJobs
234	2026-06-10 15:16:50.235386	Demo seed: Analyze financial performance and support planning decisions. Key skills: Financial Analysis,Risk Management,Excel,SQL. Job type: Contract.	Mid	Contract	Da Nang	2026-04-07 15:16:50.235386	24500000.00	Financial Analyst Mid #293	129	open	20500000	28500000	VND	2026-04-29 15:16:50.235386	\N	FINANCE	https://placehold.co/640x360/EFF6FF/2563EB?text=TTJobs
235	2026-06-11 15:16:50.235386	Demo seed: Coordinate daily operations and improve team execution. Key skills: Excel,Communication,Customer Success,Business Strategy. Job type: Internship.	Senior	Internship	Remote	2026-04-06 15:16:50.235386	26000000.00	Operations Coordinator Senior #024	130	open	22000000	30000000	VND	2026-04-29 15:16:50.235386	\N	OPERATIONS	https://placehold.co/640x360/EFF6FF/2563EB?text=TTJobs
236	2026-05-27 15:16:50.235386	Demo seed: Help customers resolve issues with clear and friendly support. Key skills: Customer Support,Communication,CRM,Customer Success. Job type: Internship.	Senior	Internship	Remote	2026-04-21 15:16:50.235386	29000000.00	Customer Support Specialist Senior #054	130	open	25000000	33000000	VND	2026-04-29 15:16:50.235386	\N	CUSTOMER-SERVICE	https://placehold.co/640x360/EFF6FF/2563EB?text=TTJobs
237	2026-06-26 15:16:50.235386	Demo seed: Coordinate daily operations and improve team execution. Key skills: Excel,Communication,Customer Success,Business Strategy. Job type: Internship.	Senior	Internship	Remote	2026-03-22 15:16:50.235386	32000000.00	Operations Coordinator Senior #084	130	open	28000000	36000000	VND	2026-04-29 15:16:50.235386	\N	OPERATIONS	https://placehold.co/640x360/EFF6FF/2563EB?text=TTJobs
238	2026-06-11 15:16:50.235386	Demo seed: Help customers resolve issues with clear and friendly support. Key skills: Customer Support,Communication,CRM,Customer Success. Job type: Internship.	Senior	Internship	Remote	2026-04-06 15:16:50.235386	24500000.00	Customer Support Specialist Senior #114	130	open	20500000	28500000	VND	2026-04-29 15:16:50.235386	\N	CUSTOMER-SERVICE	https://placehold.co/640x360/EFF6FF/2563EB?text=TTJobs
239	2026-05-27 15:16:50.235386	Demo seed: Coordinate daily operations and improve team execution. Key skills: Excel,Communication,Customer Success,Business Strategy. Job type: Internship.	Senior	Internship	Remote	2026-04-21 15:16:50.235386	27500000.00	Operations Coordinator Senior #144	130	open	23500000	31500000	VND	2026-04-29 15:16:50.235386	\N	OPERATIONS	https://placehold.co/640x360/EFF6FF/2563EB?text=TTJobs
240	2026-06-26 15:16:50.235386	Demo seed: Help customers resolve issues with clear and friendly support. Key skills: Customer Support,Communication,CRM,Customer Success. Job type: Internship.	Senior	Internship	Remote	2026-03-22 15:16:50.235386	30500000.00	Customer Support Specialist Senior #174	130	open	26500000	34500000	VND	2026-04-29 15:16:50.235386	\N	CUSTOMER-SERVICE	https://placehold.co/640x360/EFF6FF/2563EB?text=TTJobs
241	2026-06-11 15:16:50.235386	Demo seed: Coordinate daily operations and improve team execution. Key skills: Excel,Communication,Customer Success,Business Strategy. Job type: Internship.	Senior	Internship	Remote	2026-04-06 15:16:50.235386	23000000.00	Operations Coordinator Senior #204	130	open	19000000	27000000	VND	2026-04-29 15:16:50.235386	\N	OPERATIONS	https://placehold.co/640x360/EFF6FF/2563EB?text=TTJobs
242	2026-05-27 15:16:50.235386	Demo seed: Help customers resolve issues with clear and friendly support. Key skills: Customer Support,Communication,CRM,Customer Success. Job type: Internship.	Senior	Internship	Remote	2026-04-21 15:16:50.235386	26000000.00	Customer Support Specialist Senior #234	130	open	22000000	30000000	VND	2026-04-29 15:16:50.235386	\N	CUSTOMER-SERVICE	https://placehold.co/640x360/EFF6FF/2563EB?text=TTJobs
243	2026-06-26 15:16:50.235386	Demo seed: Coordinate daily operations and improve team execution. Key skills: Excel,Communication,Customer Success,Business Strategy. Job type: Internship.	Senior	Internship	Remote	2026-03-22 15:16:50.235386	29000000.00	Operations Coordinator Senior #264	130	open	25000000	33000000	VND	2026-04-29 15:16:50.235386	\N	OPERATIONS	https://placehold.co/640x360/EFF6FF/2563EB?text=TTJobs
244	2026-06-11 15:16:50.235386	Demo seed: Help customers resolve issues with clear and friendly support. Key skills: Customer Support,Communication,CRM,Customer Success. Job type: Internship.	Senior	Internship	Remote	2026-04-06 15:16:50.235386	32000000.00	Customer Support Specialist Senior #294	130	open	28000000	36000000	VND	2026-04-29 15:16:50.235386	\N	CUSTOMER-SERVICE	https://placehold.co/640x360/EFF6FF/2563EB?text=TTJobs
245	2026-06-12 15:16:50.235386	Demo seed: Build and maintain production software for growing product teams. Key skills: Java,Spring Boot,React,SQL,Docker. Job type: Full-time.	Lead	Full-time	Hybrid	2026-04-05 15:16:50.235386	33500000.00	Software Engineer Lead #025	131	open	29500000	37500000	VND	2026-04-29 15:16:50.235386	\N	INFORMATION-TECHNOLOGY	https://placehold.co/640x360/EFF6FF/2563EB?text=TTJobs
246	2026-05-28 15:16:50.235386	Demo seed: Advise customers and manage real estate opportunities. Key skills: Real Estate Sales,Property Management,Negotiation,CRM. Job type: Full-time.	Lead	Full-time	Hybrid	2026-04-20 15:16:50.235386	36500000.00	Property Consultant Lead #055	131	open	32500000	40500000	VND	2026-04-29 15:16:50.235386	\N	REAL-ESTATE	https://placehold.co/640x360/EFF6FF/2563EB?text=TTJobs
247	2026-06-27 15:16:50.235386	Demo seed: Build and maintain production software for growing product teams. Key skills: Java,Spring Boot,React,SQL,Docker. Job type: Full-time.	Lead	Full-time	Hybrid	2026-03-21 15:16:50.235386	29000000.00	Software Engineer Lead #085	131	open	25000000	33000000	VND	2026-04-29 15:16:50.235386	\N	INFORMATION-TECHNOLOGY	https://placehold.co/640x360/EFF6FF/2563EB?text=TTJobs
248	2026-06-12 15:16:50.235386	Demo seed: Advise customers and manage real estate opportunities. Key skills: Real Estate Sales,Property Management,Negotiation,CRM. Job type: Full-time.	Lead	Full-time	Hybrid	2026-04-05 15:16:50.235386	32000000.00	Property Consultant Lead #115	131	open	28000000	36000000	VND	2026-04-29 15:16:50.235386	\N	REAL-ESTATE	https://placehold.co/640x360/EFF6FF/2563EB?text=TTJobs
249	2026-05-28 15:16:50.235386	Demo seed: Build and maintain production software for growing product teams. Key skills: Java,Spring Boot,React,SQL,Docker. Job type: Full-time.	Lead	Full-time	Hybrid	2026-04-20 15:16:50.235386	35000000.00	Software Engineer Lead #145	131	open	31000000	39000000	VND	2026-04-29 15:16:50.235386	\N	INFORMATION-TECHNOLOGY	https://placehold.co/640x360/EFF6FF/2563EB?text=TTJobs
250	2026-06-27 15:16:50.235386	Demo seed: Advise customers and manage real estate opportunities. Key skills: Real Estate Sales,Property Management,Negotiation,CRM. Job type: Full-time.	Lead	Full-time	Hybrid	2026-03-21 15:16:50.235386	38000000.00	Property Consultant Lead #175	131	open	34000000	42000000	VND	2026-04-29 15:16:50.235386	\N	REAL-ESTATE	https://placehold.co/640x360/EFF6FF/2563EB?text=TTJobs
251	2026-06-12 15:16:50.235386	Demo seed: Build and maintain production software for growing product teams. Key skills: Java,Spring Boot,React,SQL,Docker. Job type: Full-time.	Lead	Full-time	Hybrid	2026-04-05 15:16:50.235386	30500000.00	Software Engineer Lead #205	131	open	26500000	34500000	VND	2026-04-29 15:16:50.235386	\N	INFORMATION-TECHNOLOGY	https://placehold.co/640x360/EFF6FF/2563EB?text=TTJobs
252	2026-05-28 15:16:50.235386	Demo seed: Advise customers and manage real estate opportunities. Key skills: Real Estate Sales,Property Management,Negotiation,CRM. Job type: Full-time.	Lead	Full-time	Hybrid	2026-04-20 15:16:50.235386	33500000.00	Property Consultant Lead #235	131	open	29500000	37500000	VND	2026-04-29 15:16:50.235386	\N	REAL-ESTATE	https://placehold.co/640x360/EFF6FF/2563EB?text=TTJobs
253	2026-06-27 15:16:50.235386	Demo seed: Build and maintain production software for growing product teams. Key skills: Java,Spring Boot,React,SQL,Docker. Job type: Full-time.	Lead	Full-time	Hybrid	2026-03-21 15:16:50.235386	36500000.00	Software Engineer Lead #265	131	open	32500000	40500000	VND	2026-04-29 15:16:50.235386	\N	INFORMATION-TECHNOLOGY	https://placehold.co/640x360/EFF6FF/2563EB?text=TTJobs
254	2026-06-12 15:16:50.235386	Demo seed: Advise customers and manage real estate opportunities. Key skills: Real Estate Sales,Property Management,Negotiation,CRM. Job type: Full-time.	Lead	Full-time	Hybrid	2026-04-05 15:16:50.235386	29000000.00	Property Consultant Lead #295	131	open	25000000	33000000	VND	2026-04-29 15:16:50.235386	\N	REAL-ESTATE	https://placehold.co/640x360/EFF6FF/2563EB?text=TTJobs
255	2026-06-13 15:16:50.235386	Demo seed: Own sales pipeline and close new business opportunities. Key skills: Sales,CRM,Negotiation,Customer Success. Job type: Part-time.	Entry	Part-time	Ha Noi	2026-04-04 15:16:50.235386	17000000.00	Sales Executive Entry #026	132	open	13000000	21000000	VND	2026-04-29 15:16:50.235386	\N	SALES	https://placehold.co/640x360/EFF6FF/2563EB?text=TTJobs
256	2026-05-29 15:16:50.235386	Demo seed: Handle accounting records, reports and compliance tasks. Key skills: Accounting,Tax,Audit,Excel. Job type: Part-time.	Entry	Part-time	Ha Noi	2026-04-19 15:16:50.235386	20000000.00	Accountant Entry #056	132	open	16000000	24000000	VND	2026-04-29 15:16:50.235386	\N	ACCOUNTING	https://placehold.co/640x360/EFF6FF/2563EB?text=TTJobs
257	2026-06-28 15:16:50.235386	Demo seed: Own sales pipeline and close new business opportunities. Key skills: Sales,CRM,Negotiation,Customer Success. Job type: Part-time.	Entry	Part-time	Ha Noi	2026-03-20 15:16:50.235386	12500000.00	Sales Executive Entry #086	132	open	8500000	16500000	VND	2026-04-29 15:16:50.235386	\N	SALES	https://placehold.co/640x360/EFF6FF/2563EB?text=TTJobs
258	2026-06-13 15:16:50.235386	Demo seed: Handle accounting records, reports and compliance tasks. Key skills: Accounting,Tax,Audit,Excel. Job type: Part-time.	Entry	Part-time	Ha Noi	2026-04-04 15:16:50.235386	15500000.00	Accountant Entry #116	132	open	11500000	19500000	VND	2026-04-29 15:16:50.235386	\N	ACCOUNTING	https://placehold.co/640x360/EFF6FF/2563EB?text=TTJobs
259	2026-05-29 15:16:50.235386	Demo seed: Own sales pipeline and close new business opportunities. Key skills: Sales,CRM,Negotiation,Customer Success. Job type: Part-time.	Entry	Part-time	Ha Noi	2026-04-19 15:16:50.235386	18500000.00	Sales Executive Entry #146	132	open	14500000	22500000	VND	2026-04-29 15:16:50.235386	\N	SALES	https://placehold.co/640x360/EFF6FF/2563EB?text=TTJobs
260	2026-06-28 15:16:50.235386	Demo seed: Handle accounting records, reports and compliance tasks. Key skills: Accounting,Tax,Audit,Excel. Job type: Part-time.	Entry	Part-time	Ha Noi	2026-03-20 15:16:50.235386	11000000.00	Accountant Entry #176	132	open	7000000	15000000	VND	2026-04-29 15:16:50.235386	\N	ACCOUNTING	https://placehold.co/640x360/EFF6FF/2563EB?text=TTJobs
261	2026-06-13 15:16:50.235386	Demo seed: Own sales pipeline and close new business opportunities. Key skills: Sales,CRM,Negotiation,Customer Success. Job type: Part-time.	Entry	Part-time	Ha Noi	2026-04-04 15:16:50.235386	14000000.00	Sales Executive Entry #206	132	open	10000000	18000000	VND	2026-04-29 15:16:50.235386	\N	SALES	https://placehold.co/640x360/EFF6FF/2563EB?text=TTJobs
262	2026-05-29 15:16:50.235386	Demo seed: Handle accounting records, reports and compliance tasks. Key skills: Accounting,Tax,Audit,Excel. Job type: Part-time.	Entry	Part-time	Ha Noi	2026-04-19 15:16:50.235386	17000000.00	Accountant Entry #236	132	open	13000000	21000000	VND	2026-04-29 15:16:50.235386	\N	ACCOUNTING	https://placehold.co/640x360/EFF6FF/2563EB?text=TTJobs
263	2026-06-28 15:16:50.235386	Demo seed: Own sales pipeline and close new business opportunities. Key skills: Sales,CRM,Negotiation,Customer Success. Job type: Part-time.	Entry	Part-time	Ha Noi	2026-03-20 15:16:50.235386	20000000.00	Sales Executive Entry #266	132	open	16000000	24000000	VND	2026-04-29 15:16:50.235386	\N	SALES	https://placehold.co/640x360/EFF6FF/2563EB?text=TTJobs
264	2026-06-13 15:16:50.235386	Demo seed: Handle accounting records, reports and compliance tasks. Key skills: Accounting,Tax,Audit,Excel. Job type: Part-time.	Entry	Part-time	Ha Noi	2026-04-04 15:16:50.235386	12500000.00	Accountant Entry #296	132	open	8500000	16500000	VND	2026-04-29 15:16:50.235386	\N	ACCOUNTING	https://placehold.co/640x360/EFF6FF/2563EB?text=TTJobs
265	2026-06-14 15:16:50.235386	Demo seed: Plan and execute digital campaigns across key channels. Key skills: SEO,Content Marketing,Google Ads,Social Media. Job type: Remote.	Junior	Remote	Ho Chi Minh City	2026-04-03 15:16:50.235386	20500000.00	Digital Marketing Specialist Junior #027	133	open	16500000	24500000	VND	2026-04-29 15:16:50.235386	\N	MARKETING	https://placehold.co/640x360/EFF6FF/2563EB?text=TTJobs
266	2026-05-30 15:16:50.235386	Demo seed: Design thoughtful user experiences for web and mobile products. Key skills: Figma,UI Design,UX Research,Communication. Job type: Remote.	Junior	Remote	Ho Chi Minh City	2026-04-18 15:16:50.235386	13000000.00	Product Designer Junior #057	133	open	9000000	17000000	VND	2026-04-29 15:16:50.235386	\N	DESIGN	https://placehold.co/640x360/EFF6FF/2563EB?text=TTJobs
267	2026-06-29 15:16:50.235386	Demo seed: Plan and execute digital campaigns across key channels. Key skills: SEO,Content Marketing,Google Ads,Social Media. Job type: Remote.	Junior	Remote	Ho Chi Minh City	2026-03-19 15:16:50.235386	16000000.00	Digital Marketing Specialist Junior #087	133	open	12000000	20000000	VND	2026-04-29 15:16:50.235386	\N	MARKETING	https://placehold.co/640x360/EFF6FF/2563EB?text=TTJobs
268	2026-06-14 15:16:50.235386	Demo seed: Design thoughtful user experiences for web and mobile products. Key skills: Figma,UI Design,UX Research,Communication. Job type: Remote.	Junior	Remote	Ho Chi Minh City	2026-04-03 15:16:50.235386	19000000.00	Product Designer Junior #117	133	open	15000000	23000000	VND	2026-04-29 15:16:50.235386	\N	DESIGN	https://placehold.co/640x360/EFF6FF/2563EB?text=TTJobs
269	2026-05-30 15:16:50.235386	Demo seed: Plan and execute digital campaigns across key channels. Key skills: SEO,Content Marketing,Google Ads,Social Media. Job type: Remote.	Junior	Remote	Ho Chi Minh City	2026-04-18 15:16:50.235386	22000000.00	Digital Marketing Specialist Junior #147	133	open	18000000	26000000	VND	2026-04-29 15:16:50.235386	\N	MARKETING	https://placehold.co/640x360/EFF6FF/2563EB?text=TTJobs
270	2026-06-29 15:16:50.235386	Demo seed: Design thoughtful user experiences for web and mobile products. Key skills: Figma,UI Design,UX Research,Communication. Job type: Remote.	Junior	Remote	Ho Chi Minh City	2026-03-19 15:16:50.235386	14500000.00	Product Designer Junior #177	133	open	10500000	18500000	VND	2026-04-29 15:16:50.235386	\N	DESIGN	https://placehold.co/640x360/EFF6FF/2563EB?text=TTJobs
271	2026-06-14 15:16:50.235386	Demo seed: Plan and execute digital campaigns across key channels. Key skills: SEO,Content Marketing,Google Ads,Social Media. Job type: Remote.	Junior	Remote	Ho Chi Minh City	2026-04-03 15:16:50.235386	17500000.00	Digital Marketing Specialist Junior #207	133	open	13500000	21500000	VND	2026-04-29 15:16:50.235386	\N	MARKETING	https://placehold.co/640x360/EFF6FF/2563EB?text=TTJobs
272	2026-05-30 15:16:50.235386	Demo seed: Design thoughtful user experiences for web and mobile products. Key skills: Figma,UI Design,UX Research,Communication. Job type: Remote.	Junior	Remote	Ho Chi Minh City	2026-04-18 15:16:50.235386	20500000.00	Product Designer Junior #237	133	open	16500000	24500000	VND	2026-04-29 15:16:50.235386	\N	DESIGN	https://placehold.co/640x360/EFF6FF/2563EB?text=TTJobs
273	2026-06-29 15:16:50.235386	Demo seed: Plan and execute digital campaigns across key channels. Key skills: SEO,Content Marketing,Google Ads,Social Media. Job type: Remote.	Junior	Remote	Ho Chi Minh City	2026-03-19 15:16:50.235386	13000000.00	Digital Marketing Specialist Junior #267	133	open	9000000	17000000	VND	2026-04-29 15:16:50.235386	\N	MARKETING	https://placehold.co/640x360/EFF6FF/2563EB?text=TTJobs
274	2026-06-14 15:16:50.235386	Demo seed: Design thoughtful user experiences for web and mobile products. Key skills: Figma,UI Design,UX Research,Communication. Job type: Remote.	Junior	Remote	Ho Chi Minh City	2026-04-03 15:16:50.235386	16000000.00	Product Designer Junior #297	133	open	12000000	20000000	VND	2026-04-29 15:16:50.235386	\N	DESIGN	https://placehold.co/640x360/EFF6FF/2563EB?text=TTJobs
275	2026-06-15 15:16:50.235386	Demo seed: Support hiring, onboarding and people operations. Key skills: Recruitment,HR Operations,Payroll,Communication. Job type: Hybrid.	Mid	Hybrid	Da Nang	2026-04-02 15:16:50.235386	26000000.00	HR Operations Specialist Mid #028	134	open	22000000	30000000	VND	2026-04-29 15:16:50.235386	\N	HR	https://placehold.co/640x360/EFF6FF/2563EB?text=TTJobs
276	2026-05-31 15:16:50.235386	Demo seed: Develop partnerships and new growth opportunities. Key skills: Partnerships,Market Research,Business Strategy,Negotiation. Job type: Hybrid.	Mid	Hybrid	Da Nang	2026-04-17 15:16:50.235386	18500000.00	Business Development Executive Mid #058	134	open	14500000	22500000	VND	2026-04-29 15:16:50.235386	\N	BUSINESS-DEVELOPMENT	https://placehold.co/640x360/EFF6FF/2563EB?text=TTJobs
277	2026-06-30 15:16:50.235386	Demo seed: Support hiring, onboarding and people operations. Key skills: Recruitment,HR Operations,Payroll,Communication. Job type: Hybrid.	Mid	Hybrid	Da Nang	2026-03-18 15:16:50.235386	21500000.00	HR Operations Specialist Mid #088	134	open	17500000	25500000	VND	2026-04-29 15:16:50.235386	\N	HR	https://placehold.co/640x360/EFF6FF/2563EB?text=TTJobs
278	2026-06-15 15:16:50.235386	Demo seed: Develop partnerships and new growth opportunities. Key skills: Partnerships,Market Research,Business Strategy,Negotiation. Job type: Hybrid.	Mid	Hybrid	Da Nang	2026-04-02 15:16:50.235386	24500000.00	Business Development Executive Mid #118	134	open	20500000	28500000	VND	2026-04-29 15:16:50.235386	\N	BUSINESS-DEVELOPMENT	https://placehold.co/640x360/EFF6FF/2563EB?text=TTJobs
279	2026-05-31 15:16:50.235386	Demo seed: Support hiring, onboarding and people operations. Key skills: Recruitment,HR Operations,Payroll,Communication. Job type: Hybrid.	Mid	Hybrid	Da Nang	2026-04-17 15:16:50.235386	17000000.00	HR Operations Specialist Mid #148	134	open	13000000	21000000	VND	2026-04-29 15:16:50.235386	\N	HR	https://placehold.co/640x360/EFF6FF/2563EB?text=TTJobs
280	2026-06-30 15:16:50.235386	Demo seed: Develop partnerships and new growth opportunities. Key skills: Partnerships,Market Research,Business Strategy,Negotiation. Job type: Hybrid.	Mid	Hybrid	Da Nang	2026-03-18 15:16:50.235386	20000000.00	Business Development Executive Mid #178	134	open	16000000	24000000	VND	2026-04-29 15:16:50.235386	\N	BUSINESS-DEVELOPMENT	https://placehold.co/640x360/EFF6FF/2563EB?text=TTJobs
281	2026-06-15 15:16:50.235386	Demo seed: Support hiring, onboarding and people operations. Key skills: Recruitment,HR Operations,Payroll,Communication. Job type: Hybrid.	Mid	Hybrid	Da Nang	2026-04-02 15:16:50.235386	23000000.00	HR Operations Specialist Mid #208	134	open	19000000	27000000	VND	2026-04-29 15:16:50.235386	\N	HR	https://placehold.co/640x360/EFF6FF/2563EB?text=TTJobs
282	2026-05-31 15:16:50.235386	Demo seed: Develop partnerships and new growth opportunities. Key skills: Partnerships,Market Research,Business Strategy,Negotiation. Job type: Hybrid.	Mid	Hybrid	Da Nang	2026-04-17 15:16:50.235386	26000000.00	Business Development Executive Mid #238	134	open	22000000	30000000	VND	2026-04-29 15:16:50.235386	\N	BUSINESS-DEVELOPMENT	https://placehold.co/640x360/EFF6FF/2563EB?text=TTJobs
283	2026-06-30 15:16:50.235386	Demo seed: Support hiring, onboarding and people operations. Key skills: Recruitment,HR Operations,Payroll,Communication. Job type: Hybrid.	Mid	Hybrid	Da Nang	2026-03-18 15:16:50.235386	18500000.00	HR Operations Specialist Mid #268	134	open	14500000	22500000	VND	2026-04-29 15:16:50.235386	\N	HR	https://placehold.co/640x360/EFF6FF/2563EB?text=TTJobs
284	2026-06-15 15:16:50.235386	Demo seed: Develop partnerships and new growth opportunities. Key skills: Partnerships,Market Research,Business Strategy,Negotiation. Job type: Hybrid.	Mid	Hybrid	Da Nang	2026-04-02 15:16:50.235386	21500000.00	Business Development Executive Mid #298	134	open	17500000	25500000	VND	2026-04-29 15:16:50.235386	\N	BUSINESS-DEVELOPMENT	https://placehold.co/640x360/EFF6FF/2563EB?text=TTJobs
285	2026-06-16 15:16:50.235386	Demo seed: Analyze financial performance and support planning decisions. Key skills: Financial Analysis,Risk Management,Excel,SQL. Job type: Contract.	Senior	Contract	Remote	2026-04-01 15:16:50.235386	23000000.00	Financial Analyst Senior #029	135	open	19000000	27000000	VND	2026-04-29 15:16:50.235386	\N	FINANCE	https://placehold.co/640x360/EFF6FF/2563EB?text=TTJobs
286	2026-06-01 15:16:50.235386	Demo seed: Design practical technical solutions for customer and internal needs. Key skills: Python,AWS,Docker,SQL. Job type: Contract.	Senior	Contract	Remote	2026-04-16 15:16:50.235386	26000000.00	Solutions Engineer Senior #059	135	open	22000000	30000000	VND	2026-04-29 15:16:50.235386	\N	ENGINEERING	https://placehold.co/640x360/EFF6FF/2563EB?text=TTJobs
287	2026-07-01 15:16:50.235386	Demo seed: Analyze financial performance and support planning decisions. Key skills: Financial Analysis,Risk Management,Excel,SQL. Job type: Contract.	Senior	Contract	Remote	2026-03-17 15:16:50.235386	29000000.00	Financial Analyst Senior #089	135	open	25000000	33000000	VND	2026-04-29 15:16:50.235386	\N	FINANCE	https://placehold.co/640x360/EFF6FF/2563EB?text=TTJobs
288	2026-06-16 15:16:50.235386	Demo seed: Design practical technical solutions for customer and internal needs. Key skills: Python,AWS,Docker,SQL. Job type: Contract.	Senior	Contract	Remote	2026-04-01 15:16:50.235386	32000000.00	Solutions Engineer Senior #119	135	open	28000000	36000000	VND	2026-04-29 15:16:50.235386	\N	ENGINEERING	https://placehold.co/640x360/EFF6FF/2563EB?text=TTJobs
289	2026-06-01 15:16:50.235386	Demo seed: Analyze financial performance and support planning decisions. Key skills: Financial Analysis,Risk Management,Excel,SQL. Job type: Contract.	Senior	Contract	Remote	2026-04-16 15:16:50.235386	24500000.00	Financial Analyst Senior #149	135	open	20500000	28500000	VND	2026-04-29 15:16:50.235386	\N	FINANCE	https://placehold.co/640x360/EFF6FF/2563EB?text=TTJobs
290	2026-07-01 15:16:50.235386	Demo seed: Design practical technical solutions for customer and internal needs. Key skills: Python,AWS,Docker,SQL. Job type: Contract.	Senior	Contract	Remote	2026-03-17 15:16:50.235386	27500000.00	Solutions Engineer Senior #179	135	open	23500000	31500000	VND	2026-04-29 15:16:50.235386	\N	ENGINEERING	https://placehold.co/640x360/EFF6FF/2563EB?text=TTJobs
291	2026-06-16 15:16:50.235386	Demo seed: Analyze financial performance and support planning decisions. Key skills: Financial Analysis,Risk Management,Excel,SQL. Job type: Contract.	Senior	Contract	Remote	2026-04-01 15:16:50.235386	30500000.00	Financial Analyst Senior #209	135	open	26500000	34500000	VND	2026-04-29 15:16:50.235386	\N	FINANCE	https://placehold.co/640x360/EFF6FF/2563EB?text=TTJobs
292	2026-06-01 15:16:50.235386	Demo seed: Design practical technical solutions for customer and internal needs. Key skills: Python,AWS,Docker,SQL. Job type: Contract.	Senior	Contract	Remote	2026-04-16 15:16:50.235386	23000000.00	Solutions Engineer Senior #239	135	open	19000000	27000000	VND	2026-04-29 15:16:50.235386	\N	ENGINEERING	https://placehold.co/640x360/EFF6FF/2563EB?text=TTJobs
293	2026-07-01 15:16:50.235386	Demo seed: Analyze financial performance and support planning decisions. Key skills: Financial Analysis,Risk Management,Excel,SQL. Job type: Contract.	Senior	Contract	Remote	2026-03-17 15:16:50.235386	26000000.00	Financial Analyst Senior #269	135	open	22000000	30000000	VND	2026-04-29 15:16:50.235386	\N	FINANCE	https://placehold.co/640x360/EFF6FF/2563EB?text=TTJobs
294	2026-06-16 15:16:50.235386	Demo seed: Design practical technical solutions for customer and internal needs. Key skills: Python,AWS,Docker,SQL. Job type: Contract.	Senior	Contract	Remote	2026-04-01 15:16:50.235386	29000000.00	Solutions Engineer Senior #299	135	open	25000000	33000000	VND	2026-04-29 15:16:50.235386	\N	ENGINEERING	https://placehold.co/640x360/EFF6FF/2563EB?text=TTJobs
295	2026-06-17 15:16:50.235386	Demo seed: Help customers resolve issues with clear and friendly support. Key skills: Customer Support,Communication,CRM,Customer Success. Job type: Internship.	Lead	Internship	Hybrid	2026-03-31 15:16:50.235386	30500000.00	Customer Support Specialist Lead #030	136	open	26500000	34500000	VND	2026-04-29 15:16:50.235386	\N	CUSTOMER-SERVICE	https://placehold.co/640x360/EFF6FF/2563EB?text=TTJobs
296	2026-06-02 15:16:50.235386	Demo seed: Coordinate daily operations and improve team execution. Key skills: Excel,Communication,Customer Success,Business Strategy. Job type: Internship.	Lead	Internship	Hybrid	2026-04-15 15:16:50.235386	33500000.00	Operations Coordinator Lead #060	136	open	29500000	37500000	VND	2026-04-29 15:16:50.235386	\N	OPERATIONS	https://placehold.co/640x360/EFF6FF/2563EB?text=TTJobs
297	2026-07-02 15:16:50.235386	Demo seed: Help customers resolve issues with clear and friendly support. Key skills: Customer Support,Communication,CRM,Customer Success. Job type: Internship.	Lead	Internship	Hybrid	2026-03-16 15:16:50.235386	36500000.00	Customer Support Specialist Lead #090	136	open	32500000	40500000	VND	2026-04-29 15:16:50.235386	\N	CUSTOMER-SERVICE	https://placehold.co/640x360/EFF6FF/2563EB?text=TTJobs
298	2026-06-17 15:16:50.235386	Demo seed: Coordinate daily operations and improve team execution. Key skills: Excel,Communication,Customer Success,Business Strategy. Job type: Internship.	Lead	Internship	Hybrid	2026-03-31 15:16:50.235386	29000000.00	Operations Coordinator Lead #120	136	open	25000000	33000000	VND	2026-04-29 15:16:50.235386	\N	OPERATIONS	https://placehold.co/640x360/EFF6FF/2563EB?text=TTJobs
299	2026-06-02 15:16:50.235386	Demo seed: Help customers resolve issues with clear and friendly support. Key skills: Customer Support,Communication,CRM,Customer Success. Job type: Internship.	Lead	Internship	Hybrid	2026-04-15 15:16:50.235386	32000000.00	Customer Support Specialist Lead #150	136	open	28000000	36000000	VND	2026-04-29 15:16:50.235386	\N	CUSTOMER-SERVICE	https://placehold.co/640x360/EFF6FF/2563EB?text=TTJobs
300	2026-07-02 15:16:50.235386	Demo seed: Coordinate daily operations and improve team execution. Key skills: Excel,Communication,Customer Success,Business Strategy. Job type: Internship.	Lead	Internship	Hybrid	2026-03-16 15:16:50.235386	35000000.00	Operations Coordinator Lead #180	136	open	31000000	39000000	VND	2026-04-29 15:16:50.235386	\N	OPERATIONS	https://placehold.co/640x360/EFF6FF/2563EB?text=TTJobs
301	2026-06-17 15:16:50.235386	Demo seed: Help customers resolve issues with clear and friendly support. Key skills: Customer Support,Communication,CRM,Customer Success. Job type: Internship.	Lead	Internship	Hybrid	2026-03-31 15:16:50.235386	38000000.00	Customer Support Specialist Lead #210	136	open	34000000	42000000	VND	2026-04-29 15:16:50.235386	\N	CUSTOMER-SERVICE	https://placehold.co/640x360/EFF6FF/2563EB?text=TTJobs
302	2026-06-02 15:16:50.235386	Demo seed: Coordinate daily operations and improve team execution. Key skills: Excel,Communication,Customer Success,Business Strategy. Job type: Internship.	Lead	Internship	Hybrid	2026-04-15 15:16:50.235386	30500000.00	Operations Coordinator Lead #240	136	open	26500000	34500000	VND	2026-04-29 15:16:50.235386	\N	OPERATIONS	https://placehold.co/640x360/EFF6FF/2563EB?text=TTJobs
303	2026-07-02 15:16:50.235386	Demo seed: Help customers resolve issues with clear and friendly support. Key skills: Customer Support,Communication,CRM,Customer Success. Job type: Internship.	Lead	Internship	Hybrid	2026-03-16 15:16:50.235386	33500000.00	Customer Support Specialist Lead #270	136	open	29500000	37500000	VND	2026-04-29 15:16:50.235386	\N	CUSTOMER-SERVICE	https://placehold.co/640x360/EFF6FF/2563EB?text=TTJobs
304	2026-06-17 15:16:50.235386	Demo seed: Coordinate daily operations and improve team execution. Key skills: Excel,Communication,Customer Success,Business Strategy. Job type: Internship.	Lead	Internship	Hybrid	2026-03-31 15:16:50.235386	36500000.00	Operations Coordinator Lead #300	136	open	32500000	40500000	VND	2026-04-29 15:16:50.235386	\N	OPERATIONS	https://placehold.co/640x360/EFF6FF/2563EB?text=TTJobs
305	2026-04-15 08:33:00	Tốt	ENTRY	Full-time	Đà Nẵng	2026-04-29 15:33:21.713952	\N	CTY	122	draft	8	26	VND	2026-04-29 15:33:25.626727	\N	INFORMATION-TECHNOLOGY	https://res.cloudinary.com/dgny2gq8p/image/upload/v1777451605/ttjobs/jobs/job-305-1777451602030.png
306	2026-05-20 15:26:00	Công nghệ thông tin, biết sử dụng java, docker...	ENTRY	Full-time	Quận Thanh Xuân	2026-04-29 19:26:48.32498	\N	Test	122	open	10	30	USD	2026-05-04 09:21:42.971144	\N	INFORMATION-TECHNOLOGY	
\.


--
-- Data for Name: message_attachments; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.message_attachments (id, message_id, file_name, file_url, public_id, mime_type, file_size, created_at) FROM stdin;
1	3	Question 4.docx	https://res.cloudinary.com/dgny2gq8p/raw/upload/v1777171656/ttjobs/messages/file_ppefi3	ttjobs/messages/file_ppefi3	application/vnd.openxmlformats-officedocument.wordprocessingml.document	71219	2026-04-26 09:47:37.158975
2	4	Question 4.docx	https://res.cloudinary.com/dgny2gq8p/raw/upload/v1777171656/ttjobs/messages/file_mfzyyj	ttjobs/messages/file_mfzyyj	application/vnd.openxmlformats-officedocument.wordprocessingml.document	71219	2026-04-26 09:47:37.483686
3	5	TTCĐ2.docx	https://res.cloudinary.com/dgny2gq8p/raw/upload/v1777172312/ttjobs/messages/file_pftlrc	ttjobs/messages/file_pftlrc	application/vnd.openxmlformats-officedocument.wordprocessingml.document	19717	2026-04-26 09:58:33.216788
4	6	BaoCao_QuangHoc_TongHop.pptx	https://res.cloudinary.com/dgny2gq8p/raw/upload/v1777172334/ttjobs/messages/file_hnmq0s	ttjobs/messages/file_hnmq0s	application/vnd.openxmlformats-officedocument.presentationml.presentation	36873	2026-04-26 09:58:55.444358
5	7	Midterm_Review_OS_ans.pdf	https://res.cloudinary.com/dgny2gq8p/image/upload/v1777172931/ttjobs/messages/file_fc3o2b.pdf	ttjobs/messages/file_fc3o2b	application/pdf	172698	2026-04-26 10:08:51.999199
6	9	Midterm_Review_OS_ans.pdf	https://res.cloudinary.com/dgny2gq8p/raw/upload/v1777173620/ttjobs/messages/file_h4eqz8	ttjobs/messages/file_h4eqz8	application/pdf	172698	2026-04-26 10:20:21.357456
7	10	BaoCao_MoPhongQuangHoc_BanDong.pptx	https://res.cloudinary.com/dgny2gq8p/raw/upload/v1777179108/ttjobs/messages/file_jpameu	ttjobs/messages/file_jpameu	application/vnd.openxmlformats-officedocument.presentationml.presentation	36699	2026-04-26 11:51:49.498201
\.


--
-- Data for Name: messages; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.messages (id, conversation_id, sender_id, content, type, created_at) FROM stdin;
1	1	6	hello bạn	text	2026-04-25 15:08:07.509335
2	1	6	hi	text	2026-04-25 22:19:37.492232
3	1	6	Đã gửi file: Question 4.docx	file	2026-04-26 09:47:35.511044
4	1	6	Đã gửi file: Question 4.docx	file	2026-04-26 09:47:33.772658
5	1	6	Đã gửi file: TTCĐ2.docx	file	2026-04-26 09:58:30.72178
6	1	6	Đã gửi file: BaoCao_QuangHoc_TongHop.pptx	file	2026-04-26 09:58:54.20958
7	1	6	Đã gửi file: Midterm_Review_OS_ans.pdf	file	2026-04-26 10:08:49.011529
8	1	6	http://localhost:5173/api/conversations/1/attachments/3/download http://localhost:5173/api/conversations/1/attachments/5/download	text	2026-04-26 10:12:26.943429
9	1	6	Đã gửi file: Midterm_Review_OS_ans.pdf	file	2026-04-26 10:20:17.525108
10	1	6	Đã gửi file: BaoCao_MoPhongQuangHoc_BanDong.pptx	file	2026-04-26 11:51:46.591818
11	1	1	chòa bạn	text	2026-04-27 15:09:50.281593
12	1	1	mình liên hệ lại	text	2026-04-27 15:10:20.918572
13	1	1	hi	text	2026-04-27 18:14:50.959834
14	1	1	1231	text	2026-04-27 18:41:40.436326
15	1	1	chào bạn	text	2026-04-27 18:41:48.851636
16	1	6	chào	text	2026-04-27 18:41:55.777932
17	1	1	chào nhà tuyển dụng	text	2026-04-27 19:57:54.535953
18	2	6	hi	text	2026-04-27 20:20:31.878706
19	3	6	chào bạn	text	2026-04-27 21:05:03.559051
20	3	6	hi	text	2026-04-27 21:25:45.420614
21	2	6	a	text	2026-04-28 07:39:41.022238
\.


--
-- Data for Name: notification_preferences; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.notification_preferences (user_id, in_app_enabled, email_enabled, created_at) FROM stdin;
1	t	t	2026-04-27 18:51:57.916401
177	t	t	2026-04-15 22:39:25.5949
\.


--
-- Data for Name: notifications; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.notifications (id, user_id, title, content, type, is_read, created_at, target_url) FROM stdin;
1	1	Application status updated	Your application for Java Backend Developer is now reviewing	APPLICATION_STATUS_UPDATED	f	2026-04-25 12:56:19.668019	\N
2	1	New message	You have a new message from Thinh	CHAT_MESSAGE	f	2026-04-25 15:08:07.518287	\N
3	1	New message	You have a new message from Thinh	CHAT_MESSAGE	f	2026-04-25 22:19:37.505143	\N
4	1	New message	You have a new message from Thinh	CHAT_MESSAGE	f	2026-04-26 09:47:37.210774	\N
5	1	New message	You have a new message from Thinh	CHAT_MESSAGE	f	2026-04-26 09:47:37.494126	\N
6	1	New message	You have a new message from Thinh	CHAT_MESSAGE	f	2026-04-26 09:58:33.227115	\N
7	1	New message	You have a new message from Thinh	CHAT_MESSAGE	f	2026-04-26 09:58:55.454512	\N
8	1	New message	You have a new message from Thinh	CHAT_MESSAGE	f	2026-04-26 10:08:52.007174	\N
9	1	New message	You have a new message from Thinh	CHAT_MESSAGE	f	2026-04-26 10:12:26.951949	\N
10	1	New message	You have a new message from Thinh	CHAT_MESSAGE	f	2026-04-26 10:20:21.381072	\N
11	1	New message	You have a new message from Thinh	CHAT_MESSAGE	f	2026-04-26 11:51:49.521113	\N
17	1	New message	You have a new message from Thinh	CHAT_MESSAGE	f	2026-04-27 18:41:55.777932	\N
19	1	Application submitted	You have successfully applied to Tuyển dụng cho vị trí trưởng phòng	APPLICATION_SUBMITTED	f	2026-04-27 20:01:16.250075	\N
20	121	New job application	Nguyễn Thành Thịnh applied to Tuyển dụng cho vị trí trưởng phòng	NEW_APPLICATION	f	2026-04-27 20:01:16.254077	\N
18	6	Tin nhắn mới	Bạn có tin nhắn mới từ Nguyễn Thành Thịnh	CHAT_MESSAGE	t	2026-04-27 19:57:54.546109	\N
21	1	Application status updated	Your application for Tuyển dụng cho vị trí trưởng phòng is now reviewing	APPLICATION_STATUS_UPDATED	f	2026-04-27 20:17:43.808868	\N
22	1	Application status updated	Your application for Tuyển dụng cho vị trí trưởng phòng is now interviewed	APPLICATION_STATUS_UPDATED	f	2026-04-27 20:17:46.201859	\N
23	1	Application status updated	Your application for Tuyển dụng cho vị trí trưởng phòng is now offered	APPLICATION_STATUS_UPDATED	f	2026-04-27 20:17:47.724073	\N
24	1	Application status updated	Your application for Tuyển dụng cho vị trí trưởng phòng is now hired	APPLICATION_STATUS_UPDATED	f	2026-04-27 20:17:50.157988	\N
25	234	Application submitted	You have successfully applied to Tuyển dụng cho vị trí trưởng phòng	APPLICATION_SUBMITTED	f	2026-04-27 20:20:12.290879	\N
26	121	New job application	ga2 applied to Tuyển dụng cho vị trí trưởng phòng	NEW_APPLICATION	f	2026-04-27 20:20:12.292924	\N
27	234	Tin nhắn mới	Bạn có tin nhắn mới từ Thinh	CHAT_MESSAGE	f	2026-04-27 20:20:31.883811	/recruiter/chat?conversationId=2
28	234	Application submitted	You have successfully applied to Java Backend Developer	APPLICATION_SUBMITTED	f	2026-04-27 20:20:48.379773	\N
29	234	Application submitted	You have successfully applied to Java Backend Developer	APPLICATION_SUBMITTED	f	2026-04-27 20:37:34.302618	\N
30	159	Application submitted	You have successfully applied to Java Backend Developer	APPLICATION_SUBMITTED	f	2026-04-27 20:38:02.965248	\N
31	159	Tin nhắn mới	Bạn có tin nhắn mới từ Thinh	CHAT_MESSAGE	f	2026-04-27 21:05:03.562557	/recruiter/chat?conversationId=3
32	159	Tin nhắn mới	Bạn có tin nhắn mới từ Thinh	CHAT_MESSAGE	f	2026-04-27 21:25:45.424906	/recruiter/chat?conversationId=3
16	6	New message	You have a new message from null	CHAT_MESSAGE	t	2026-04-27 18:41:48.866684	\N
15	6	New message	You have a new message from null	CHAT_MESSAGE	t	2026-04-27 18:41:40.445784	\N
14	6	New message	You have a new message from null	CHAT_MESSAGE	t	2026-04-27 18:14:50.976519	\N
13	6	New message	You have a new message from null	CHAT_MESSAGE	t	2026-04-27 15:10:20.922588	\N
12	6	New message	You have a new message from null	CHAT_MESSAGE	t	2026-04-27 15:09:50.289137	\N
33	234	Tin nhắn mới	Bạn có tin nhắn mới từ Thinh	CHAT_MESSAGE	f	2026-04-28 07:39:41.072069	/recruiter/chat?conversationId=2
34	234	Application submitted	You have successfully applied to Test	APPLICATION_SUBMITTED	f	2026-05-01 21:26:05.840685	\N
35	159	Application status updated	Your application for Java Backend Developer is now reviewing	APPLICATION_STATUS_UPDATED	f	2026-05-01 21:28:47.697267	\N
36	159	Application status updated	Your application for Java Backend Developer is now shortlisted	APPLICATION_STATUS_UPDATED	f	2026-05-01 21:28:49.849304	\N
37	1	Application status updated	Your application for Java Backend Developer is now shortlisted	APPLICATION_STATUS_UPDATED	f	2026-05-03 19:22:07.304072	\N
38	177	Application submitted	You have successfully applied to Test	APPLICATION_SUBMITTED	f	2026-05-04 07:31:42.880144	\N
39	177	Application submitted	You have successfully applied to Financial Analyst Lead #245	APPLICATION_SUBMITTED	f	2026-05-04 09:11:54.683139	\N
\.


--
-- Data for Name: recruiter_activity_logs; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.recruiter_activity_logs (id, actor_id, company_id, job_id, application_id, action_type, title, details, created_at) FROM stdin;
1	6	2	3	2	APPLICATION_STATUS_CHANGED	Duyệt CV	Đang cập nhật - Java Backend Developer: submitted -> reviewing	2026-04-25 12:56:19.665018
2	6	\N	\N	\N	LOGIN_SUCCESS	Đăng nhập thành công	Thinh đã đăng nhập vào workspace.	2026-04-25 17:33:06.327837
3	6	\N	\N	\N	LOGIN_SUCCESS	Đăng nhập thành công	Thinh đã đăng nhập vào workspace.	2026-04-25 22:19:28.928476
4	6	\N	\N	\N	LOGIN_SUCCESS	Đăng nhập thành công	Thinh đã đăng nhập vào workspace.	2026-04-26 09:36:23.273494
5	6	\N	\N	\N	LOGIN_SUCCESS	Đăng nhập thành công	Thinh đã đăng nhập vào workspace.	2026-04-27 14:38:37.0997
6	6	\N	\N	\N	LOGIN_SUCCESS	Đăng nhập thành công	Thinh đã đăng nhập vào workspace.	2026-04-27 18:13:08.294407
7	6	35	4	\N	JOB_UPDATED	Cập nhật job	Đã cập nhật job Tuyển dụng cho vị trí trưởng phòng tại company-11365354769000 (open)	2026-04-27 20:00:55.303275
8	6	35	4	3	CV_VIEWED	Xem CV ứng viên	Nguyễn Thành Thịnh - Tuyển dụng cho vị trí trưởng phòng đã được mở CV.	2026-04-27 20:13:06.036511
9	6	35	4	3	CV_VIEWED	Xem CV ứng viên	Nguyễn Thành Thịnh - Tuyển dụng cho vị trí trưởng phòng đã được mở CV.	2026-04-27 20:13:07.178988
10	6	35	4	3	CV_VIEWED	Xem CV ứng viên	Nguyễn Thành Thịnh - Tuyển dụng cho vị trí trưởng phòng đã được mở CV.	2026-04-27 20:17:36.070861
11	6	35	4	3	APPLICATION_STATUS_CHANGED	Duyệt CV	Nguyễn Thành Thịnh - Tuyển dụng cho vị trí trưởng phòng: submitted -> reviewing	2026-04-27 20:17:43.806846
12	6	35	4	3	APPLICATION_STATUS_CHANGED	Duyệt CV	Nguyễn Thành Thịnh - Tuyển dụng cho vị trí trưởng phòng: reviewing -> interviewed	2026-04-27 20:17:46.197672
13	6	35	4	3	APPLICATION_STATUS_CHANGED	Duyệt CV	Nguyễn Thành Thịnh - Tuyển dụng cho vị trí trưởng phòng: interviewed -> offered	2026-04-27 20:17:47.722102
14	6	35	4	3	APPLICATION_STATUS_CHANGED	Duyệt CV	Nguyễn Thành Thịnh - Tuyển dụng cho vị trí trưởng phòng: offered -> hired	2026-04-27 20:17:50.156658
15	6	\N	\N	\N	LOGIN_SUCCESS	Đăng nhập thành công	Thinh đã đăng nhập vào workspace.	2026-04-29 09:55:21.324884
16	6	122	305	\N	JOB_CREATED	Tạo job mới	Đã tạo job CTY tại AnswerHub Support (draft)	2026-04-29 15:33:21.782942
17	6	122	305	\N	JOB_UPDATED	Cập nhật job	Đã cập nhật job CTY tại AnswerHub Support (draft)	2026-04-29 15:33:25.64265
18	6	122	306	\N	JOB_CREATED	Tạo job mới	Đã tạo job Test tại AnswerHub Support (open)	2026-04-29 19:26:48.35318
19	6	122	306	\N	JOB_UPDATED	Cập nhật job	Đã cập nhật job Test tại AnswerHub Support (open)	2026-04-29 19:26:51.591435
20	6	\N	\N	\N	LOGIN_SUCCESS	Đăng nhập thành công	Thinh đã đăng nhập vào workspace.	2026-05-01 21:23:12.385896
21	6	122	306	\N	JOB_UPDATED	Cập nhật job	Đã cập nhật job Test tại AnswerHub Support (open)	2026-05-01 21:25:59.916243
22	6	1	2	7	CV_VIEWED	Xem CV ứng viên	Hoang Dat - Java Backend Developer đã được mở CV.	2026-05-01 21:27:55.766249
23	6	1	2	7	APPLICATION_STATUS_CHANGED	Duyệt CV	Hoang Dat - Java Backend Developer: submitted -> reviewing	2026-05-01 21:28:47.692071
24	6	1	2	7	APPLICATION_STATUS_CHANGED	Duyệt CV	Hoang Dat - Java Backend Developer: reviewing -> shortlisted	2026-05-01 21:28:49.84575
25	6	\N	\N	\N	LOGIN_SUCCESS	Đăng nhập thành công	Thinh đã đăng nhập vào workspace.	2026-05-03 14:48:36.083748
26	6	122	306	8	CV_VIEWED	Xem CV ứng viên	ga2 - Test đã được mở CV.	2026-05-03 14:49:00.606418
27	6	2	3	2	APPLICATION_STATUS_CHANGED	Duyệt CV	Nguyễn Thành Thịnh - Java Backend Developer: reviewing -> shortlisted	2026-05-03 19:22:07.297978
28	6	122	306	8	CV_VIEWED	Xem CV ứng viên	ga2 - Test đã được mở CV.	2026-05-04 07:29:14.194791
29	6	122	306	\N	JOB_UPDATED	Cập nhật job	Đã cập nhật job Test tại AnswerHub Support (open)	2026-05-04 07:31:12.995061
30	6	122	306	9	CV_VIEWED	Xem CV ứng viên	Hoang Dat - Test đã được mở CV.	2026-05-04 07:32:17.098344
31	6	122	306	\N	JOB_UPDATED	Cập nhật job	Đã cập nhật job Test tại AnswerHub Support (open)	2026-05-04 09:21:42.980199
32	6	\N	\N	\N	LOGIN_SUCCESS	Đăng nhập thành công	Thinh đã đăng nhập vào workspace.	2026-05-05 06:38:16.073844
33	6	\N	\N	\N	LOGIN_SUCCESS	Đăng nhập thành công	Thinh đã đăng nhập vào workspace.	2026-05-06 07:55:50.390438
\.


--
-- Data for Name: recruitment_campaign_applications; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.recruitment_campaign_applications (campaign_id, application_id) FROM stdin;
\.


--
-- Data for Name: recruitment_campaign_jobs; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.recruitment_campaign_jobs (campaign_id, job_id) FROM stdin;
1	305
1	156
1	159
1	162
1	306
\.


--
-- Data for Name: recruitment_campaigns; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.recruitment_campaigns (id, company_id, created_by_id, name, description, status, target_hires, starts_at, ends_at, created_at, updated_at) FROM stdin;
1	122	6	Văn phòng phát triển	tuyển thành viên mới	active	7	2026-05-01 00:27:00	2026-06-04 00:27:00	2026-05-04 07:27:51.515365	2026-05-04 07:27:51.515365
\.


--
-- Data for Name: recruitment_events; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.recruitment_events (id, company_id, job_id, application_id, actor_id, event_type, created_at, metadata) FROM stdin;
\.


--
-- Data for Name: saved_jobs; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.saved_jobs (id, user_id, job_id, saved_at, note, tag) FROM stdin;
1	177	3	2026-04-15 21:12:37.69231	\N	\N
2	177	2	2026-04-16 11:48:26.474113	\N	\N
\.


--
-- Data for Name: skills; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.skills (id, name) FROM stdin;
1	Market Research
2	Business Strategy
3	Property Management
4	SQL
5	Negotiation
6	Sales
7	Tax
8	Google Ads
9	Risk Management
10	CRM
11	React
12	Docker
13	UI Design
14	HR Operations
15	Excel
16	Customer Success
17	Financial Analysis
18	UX Research
19	Python
20	Figma
21	Customer Support
22	Content Marketing
23	Communication
24	Audit
25	SEO
26	Accounting
27	Partnerships
28	Payroll
29	Spring Boot
30	Social Media
31	Recruitment
32	AWS
33	Real Estate Sales
34	Java
35	Node.js
36	Github
\.


--
-- Data for Name: user_cvs; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.user_cvs (id, user_id, cv_url, file_name, uploaded_at) FROM stdin;
1	1	https://res.cloudinary.com/dgny2gq8p/raw/upload/v1777294876/ttjobs/cv/applications/app-1-1777294871649	Midterm_Review_OS_ans.pdf	2026-04-27 20:01:16.232733
2	234	https://res.cloudinary.com/dgny2gq8p/raw/upload/v1777296012/ttjobs/cv/applications/app-234-1777296008065	SP(3)2025_Midterm- Ownership and Permissions+ System and User Security .pdf	2026-04-27 20:20:12.282711
3	159	https://res.cloudinary.com/dgny2gq8p/raw/upload/v1777297082/ttjobs/cv/applications/app-159-1777297079855	TTCĐ2.docx	2026-04-27 20:38:02.958671
4	234	https://res.cloudinary.com/dgny2gq8p/raw/upload/v1777645564/ttjobs/cv/applications/app-234-1777645561488	Midterm_Review_OS_ans.pdf	2026-05-01 21:26:05.822658
5	177	https://res.cloudinary.com/dgny2gq8p/raw/upload/v1777854703/ttjobs/cv/applications/app-177-1777854699907	10089434.pdf	2026-05-04 07:31:42.865373
6	177	https://res.cloudinary.com/dgny2gq8p/raw/upload/v1777857815/ttjobs/cv/user-177-1777857812724	10247517.pdf	2026-05-04 08:23:35.801406
\.


--
-- Data for Name: user_skills; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.user_skills (user_id, skill_id) FROM stdin;
177	34
177	19
177	12
177	36
\.


--
-- Data for Name: user_tool_sessions; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.user_tool_sessions (id, user_id, tool_slug, input_json, result_json, created_at) FROM stdin;
\.


--
-- Data for Name: users; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.users (id, email, name, password_hash, role, address, avatar_url, created_at, cv_url, experience_years, phone, updated_at, cv_text, cv_role, cv_objective, cv_experience_highlights, primary_cv_type, mbti_type, mbti_taken_at, mi_scores_json, mi_taken_at, personality_public) FROM stdin;
1	thinh@gmail.com	Nguyễn Thành Thịnh	$2a$10$zx0CoYdEhyma3tl60H4dgOCNrTsxcKky70huqJjgd3rBuxL227f36	CANDIDATE		\N	\N	\N	\N		\N	CHAPTER 1 \r\n1. Question 1: \r\nWhat is an operating system?   \r\nA. A collection of hardware devices that make up a computer system   \r\nB. Software that manages both computer hardware and software resources \r\nC. A programming language used for writing code and developing applications   \r\nD. An internet browser that allows users to access online websites and services \r\nAnswer: B \r\n \r\n2. Question 2: \r\nWhich of the following is not a function of an operating system? \r\nA. Memory management \r\nB. Process management \r\nC. Hardware manufacturing \r\nD. File management \r\nAnswer: C \r\n \r\n3. Question 3: \r\nWhich of these operating systems is designed mainly for smartphones? \r\nA. Windows \r\nB. Linux \r\nC. Android \r\nD. UNIX \r\nAnswer: C \r\n \r\n4. Question 4: \r\nThe part of an operating system that directly interacts with the hardware is called: \r\nA. User interface \r\nB. Kernel \r\nC. Compiler \r\nD. File system \r\nAnswer: B \r\n \r\n5. Question 5: \r\nWhat is the main purpose of an operating system? \r\nA. To provide an environment that allows users to execute and manage programs \r\nB. To design and develop computer hardware components used in the system   \r\nC. To connect computers and manage communication over the internet network   \r\nD. To compile source code and translate it into executable programming languages \r\nAnswer: A \r\n \r\n6. Question 6: \r\nWhich of the following is an example of a multi-user operating system? \r\nA. MS-DOS \r\nB. Windows 95 \r\nC. UNIX \r\nD. Android \r\nAnswer: C \r\n \r\n7. Question 7: \r\nWhich operating system is open-source and widely used for servers? \r\nA. macOS \r\nB. Windows \r\nC. Linux \r\nD. iOS \r\nAnswer: C \r\n \r\n8. Question 8: \r\nWhat does “booting” refer to in an operating system? \r\nA. Shutting down the computer \r\nB. Starting up the computer & loading the OS \r\nC. Installing software  \r\nD. Formatting the hard drive \r\nAnswer: B \r\n \r\n9. Question 9: \r\nThe user interface that uses icons and windows is called __________. \r\nA. Command-Line Interface (CLI) \r\nB. Graphical User Interface (GUI) \r\nC. Batch Interface \r\nD. Text Interface \r\nAnswer: B \r\n \r\n10. Question 10: \r\nWhich of the following is not a type of operating system? \r\nA. Real-time OS \r\nB. Batch OS \r\nC. Time-sharing OS \r\nD. Compiler OS \r\nAnswer: D \r\n \r\n11. Question 11: \r\nWhich of the following best describes a thread? \r\nA. A complete and independent program executed by the system   \r\nB. The smallest unit of CPU scheduling within a process   \r\nC. A regular file stored and managed in the operating system   \r\nD. A hardware interrupt signal generated by computer devices  \r\nAnswer: B \r\n \r\n12. Question 12: \r\nWhat are system resources in an operating system? \r\nA. Only memory and CPU used by the system   \r\nB. Hardware and software components managed by the OS   \r\nC. Network devices and related connections controlled by the OS   \r\nD. Application programs that interact with system resources   \r\nAnswer: B \r\n \r\n13. Question 13: \r\nWhich of the following is not considered a system resource? \r\nA. CPU time \r\nB. Memory \r\nC. Files \r\nD. Web browser \r\nAnswer: D \r\n \r\n14. Question 14: \r\nThe process control block (PCB) contains which of the following types of \r\ninformation?   \r\nA. The file directory structure and paths used by the operating system   \r\nB. A process’s current state, program counter, CPU registers, and related details   \r\nC. The list of device drivers installed and managed by the operating system   \r\nD. The network configuration and communication parameters of the computer \r\nAnswer: B \r\n \r\n15. Question 15: \r\nWhat is a system call? \r\nA. A request made by a process to the operating system for a service \r\nB. A direct hardware interrupt \r\nC. A programming language function \r\nD. A communication between two processes \r\nAnswer: A \r\n \r\n16. Question 16: \r\nWhich of the following best describes the relationship between a process and its \r\nthreads? \r\nA. Threads share the same memory and system resources of a single process   \r\nB. Each thread operates independently inside its own process environment   \r\nC. Threads are unable to share any data or memory between executions   \r\nD. A process may contain only one thread that performs all operations  \r\nAnswer: A \r\n \r\n17. Question 17: \r\nWhat happens during a context switch? \r\nA. The CPU begins executing a different instruction from the same program   \r\nB. The OS saves the current state of one process and loads another process to run   \r\nC. The computer system performs a restart operation to refresh all running tasks   \r\nD. A user logs out of the current session and ends all active programs  \r\nAnswer: B \r\n \r\n18. Question 18: \r\nWhich part of the operating system executes privileged instructions? \r\nA. User mode \r\nB. Kernel mode \r\nC. Application mode \r\nD. I/O mode \r\nAnswer: B \r\n \r\n19. Question 19: \r\nHow does a process usually request access to a hardware device in an operating \r\nsystem? \r\nA. Direct access to the hardware without any mediation   \r\nB. A system call that is handled and managed by the kernel   \r\nC. A user-level function executed within the application program   \r\nD. The file manager that organizes files and device resources  \r\nAnswer: B \r\n \r\n20. Question 20: \r\nWhich type of operating system executes a batch of jobs without user interaction? \r\nA. Real-time OS \r\nB. Batch OS \r\nC. Time-sharing OS \r\nD. Distributed OS \r\nAnswer: B \r\n \r\n21. Question 21: \r\nIn which type of operating system do multiple users share system resources \r\nsimultaneously? \r\nA. Single-user OS \r\nB. Time-sharing OS \r\nC. Batch OS \r\nD. Real-time OS \r\nAnswer: B \r\n \r\n22. Question 22: \r\nWhich operating system type is used when tasks must be completed within strict \r\ntime limits? \r\nA. Real-time OS \r\nB. Distributed OS \r\nC. Batch OS \r\nD. Network OS \r\nAnswer: A \r\n \r\n23. Question 23: \r\nA distributed operating system is mainly designed to: \r\nA. Manage one single computer system \r\nB. Connect & manage multiple computers as one system \r\nC. Run only on mobile devices \r\nD. Execute one job at a time \r\nAnswer: B \r\n \r\n24. Question 24: \r\nWhich of the following is an example of a network operating system? \r\nA. Windows Server \r\nB. Android \r\nC. macOS \r\nD. MS-DOS \r\nAnswer: A \r\n \r\n25. Question 25: \r\nWhich of the following is a primary function of an operating system? \r\nA. Compiling source code \r\nB. Managing hardware & software resources \r\nC. Designing hardware circuits \r\nD. Creating computer networks \r\nAnswer: B \r\n \r\n26. Question 26: \r\nWhich operating system function keeps track of where programs and data are \r\nstored in memory? \r\nA. File management \r\nB. Memory management \r\nC. Process scheduling \r\nD. Device management \r\nAnswer: B \r\n \r\n27. Question 27: \r\nThe operating system function responsible for controlling input and output devices \r\nis called: \r\nA. Device management \r\nB. File management \r\nC. Network management \r\nD. Process management \r\nAnswer: A \r\n \r\n28. Question 28: \r\nWhich OS function is responsible for allocating the CPU to various processes? \r\nA. Memory management \r\nB. Process scheduling \r\nC. Security management \r\nD. File management \r\nAnswer: B \r\n \r\n29. Question 29: \r\nWhat is the main goal of file management in an operating system? \r\nA. To manage file creation, deletion, & access \r\nB. To install applications \r\nC. To monitor CPU temperature \r\nD. To control peripheral devices \r\nAnswer: A \r\n \r\nCHAPTER 2 \r\n30. Question 1: \r\nWhich scheduling algorithm gives the CPU to the process that arrives first? \r\nA. Shortest Job Next (SJN) \r\nB. First-Come, First-Served (FCFS) \r\nC. Round Robin (RR) \r\nD. Priority Scheduling \r\nAnswer: B \r\n \r\n31. Question 2: \r\nWhich scheduling algorithm assigns a fixed time unit per process in a cyclic \r\norder? \r\nA. FCFS \r\nB. SJF \r\nC. Round Robin (RR) \r\nD. Priority Scheduling \r\nAnswer: C \r\n \r\n32. Question 3: \r\nIn Shortest Job First (SJF) scheduling, which process is selected next? \r\nA. The one with the highest priority \r\nB. The one with the shortest CPU burst time \r\nC. The one that arrived first \r\nD. The one with the longest waiting time \r\nAnswer: B \r\n \r\n33. Question 4: \r\nIn Priority Scheduling, if two processes have the same priority, how is the tie \r\nusually broken? \r\nA. Randomly  \r\nB. Based on process ID \r\nC. By arrival time (FCFS) \r\nD. By memory usage \r\nAnswer: C \r\n \r\n34. Question 5: \r\nWhich of the following scheduling algorithms can cause starvation if not properly \r\nmanaged? \r\nA. FCFS (system term) \r\nB. Round Robin \r\nC. Priority Scheduling \r\nD. Shortest Remaining Time First (SRTF) \r\nAnswer: C \r\n \r\n35. Question 6: \r\nWhat is a process in an operating system? \r\nA. A program that is being executed \r\nB. A system file in storage \r\nC. A hardware component \r\nD. A type of system call \r\nAnswer: A \r\n \r\n36. Question 7: \r\nWhich of the following is not a valid process state? \r\nA. New \r\nB. Running \r\nC. Waiting \r\nD. Compiling \r\nAnswer: D \r\n \r\n37. Question 8: \r\nThe structure that stores all information about a process is called: \r\nA. Process Control Block (PCB) \r\nB. Process Table \r\nC. Stack Pointer \r\nD. Ready Queue \r\nAnswer: A \r\n \r\n38. Question 9: \r\nDuring a context switch, what action is performed by the operating system?   \r\nA. It compiles user programs into machine-level instructions for execution   \r\nB. It saves the current state of one process and loads another process to run   \r\nC. It removes inactive or old processes completely from the system memory   \r\nD. It allocates additional disk space required for process storage operations \r\nAnswer: B \r\n \r\n39. Question 10: \r\nIn a multiprogramming system, processes waiting to use the CPU are kept in: \r\nA. Job queue \r\nB. Ready queue \r\nC. Device queue \r\nD. File queue \r\nAnswer: B \r\n \r\n40. Question 11: \r\nWhich of the following is true about process scheduling? \r\nA. It determines which process will use the CPU next \r\nB. It is handled by the compiler \r\nC. It only occurs in single-user systems \r\nD. It is unrelated to CPU time management \r\nAnswer: A \r\n \r\n41. Question 12: \r\nThe process state changes from Running to Waiting when: \r\nA. The process completes execution \r\nB. The process requests an I/O operation \r\nC. A higher-priority process arrives \r\nD. The CPU fails (system term) \r\nAnswer: B \r\n \r\n42. Question 13: \r\nWhich of the following is not a type of process scheduler? \r\nA. Long-term scheduler \r\nB. Medium-term scheduler \r\nC. Short-term scheduler \r\nD. Hardware scheduler \r\nAnswer: D \r\n \r\n43. Question 14: \r\nWhen a child process is created using a system call such as fork(), what happens?   \r\nA. It shares the same memory and address space with its parent process entirely   \r\nB. It is almost identical to the parent process except for having a unique PID value   \r\nC. It terminates or destroys the parent process immediately after its creation   \r\nD. It runs only in kernel mode and performs no user-level operations   \r\nAnswer: B \r\n \r\n44. Question 15: \r\nWhat is the purpose of process synchronization? \r\nA. To manage the speed of the CPU \r\nB. To ensure orderly execution when processes share resources \r\nC. To increase process priority (system term) \r\nD. To convert processes into threads \r\nAnswer: B \r\n \r\n45. Question 16: \r\nIn the Shortest Remaining Time First (SRTF) scheduling algorithm, what happens \r\nif a new process arrives with a shorter burst time than the one currently running?   \r\nA. The CPU continues executing the currently running process until it finishes \r\ncompletely   \r\nB. The CPU suspends the current process and switches to execute the new shorter \r\nprocess   \r\nC. The CPU executes both processes simultaneously using shared time and \r\nresources   \r\nD. The CPU ignores the new process until the current process has completed \r\nexecution   \r\nAnswer: B \r\n \r\n46. Question 17: \r\nUsing Priority (non-preemptive) algorithm, find the order of execution for the \r\nfollowing processes with the given data in the order Process : Burst Time : \r\nPriority (0 is the highest priority) respectively A : 4 : 4; B : 1 : 1; C : 6 : 3; D : 4 : \r\n6. \r\nA. B, C, D, A \r\nB. B, C, A, D \r\nC. B, A, C, D \r\nD. B, D, A, C \r\nANSWER: B \r\n \r\n47. Question 18: \r\nUsing RR (Round-Robin) algorithm with time quantum: 3, find the average \r\nwaiting time for the following processes with the given data in the order Process \r\n: Arrival Time : Burst Time respectively A : 8 : 3; B : 0 : 5; C : 2 : 4. \r\nA. 2.333 \r\nB. 1.333 \r\nC. 5.333 \r\nD. 4.333 \r\nANSWER: A \r\n \r\n48. Question 19: \r\nUsing SJF (non-preemptive) algorithm, find the order of execution for the \r\nfollowing processes with the given data in the order Process : Arrival Time : \r\nBurst Time respectively A : 6 : 3; B : 7 : 2; C : 0 : 4; D : 2 : 7. \r\nA. C, D, B, A \r\nB. C, D, A, B \r\nC. C, B, D, A \r\nD. C, A, B, D \r\nANSWER: A \r\n \r\n49. Question 20: \r\nUsing FCFS (First Come First Served) algorithm, find the order of execution \r\nfor the following processes with the given data in the order Process : Arrival \r\nTime : Burst Time respectively A : 6 : 3; B : 7 : 2; C : 0 : 4; D : 2 : 7. \r\nA. C, B, A, D \r\nB. C, A, D, B \r\nC. C, D, B, A \r\nD. C, D, A, B \r\nANSWER: D \r\n \r\n \r\nCHAPTER 3 \r\n50. Question 1: \r\nWhat is the main purpose of memory management in an operating system? \r\nA. To organize CPU scheduling \r\nB. To allocate & deallocate memory space efficiently \r\nC. To manage input/output devices \r\nD. To protect data from viruses \r\nAnswer: B \r\n51. Question 2: \r\nWhich type of memory is directly accessible by the CPU? \r\nA. Secondary memory \r\nB. Cache memory \r\nC. Virtual memory \r\nD. Optical storage \r\nAnswer: B \r\n \r\n52. Question 3: \r\nThe technique of keeping only part of a program in memory is called__________. \r\nA. Swapping \r\nB. Paging \r\nC. Segmentation \r\nD. Virtual memory \r\nANSWER: D \r\n \r\n53. Question 3: \r\nIn paging, the memory is divided into fixed-size blocks called _______________. \r\nA. Frames \r\nB. Segments \r\nC. Pages \r\nD. Both A and C \r\nANSWER: D \r\n \r\n54. Question 4: \r\nIn a segmentation memory management system, how is memory divided? \r\nA. Into equal-sized memory blocks used for process allocation \r\nB. Into logical divisions such as functions, stacks, or data structures \r\nC. Into fixed-size pages defined by the hardware memory unit \r\nD. Into cache lines that temporarily store frequently used instructions \r\nAnswer: B \r\n \r\n55. Question 5: \r\nWhat is the process of moving a program temporarily from main memory to \r\nsecondary storage called? \r\nA. Spooling of input and output operations \r\nB. Swapping between main memory and secondary storage devices \r\nC. Paging operations that divide memory into frames and pages \r\nD. Segmentation of logical memory into distinct sections \r\nAnswer: B \r\n \r\n56. Question 6: \r\nWhat does the term internal fragmentation refer to in memory management? \r\nA. Wasted space inside allocated memory blocks that remains unused \r\nB. Unused free memory located between allocated memory regions \r\nC. A shortage of available memory for new process allocation \r\nD. Overlapping memory segments that cause data corruption \r\nAnswer: A \r\n \r\n57. Question 7: \r\nWhich of the following algorithms can be used for page replacement in operating \r\nsystems? \r\nA. First-In, First-Out (FIFO) algorithm used for page management \r\nB. Least Recently Used (LRU) algorithm that tracks page references \r\nC. Optimal algorithm that replaces the page not needed for the longest time \r\nD. All of the above algorithms are used for page replacement \r\nAnswer: D \r\n \r\n58. Question 8: \r\nWhat is the main purpose of the page table in a memory management system? \r\nA. To store process identifiers for CPU scheduling operations \r\nB. To map logical addresses generated by a program to physical addresses in \r\nmemory \r\nC. To maintain CPU scheduling and process prioritization data \r\nD. To control input and output operations of storage devices \r\nAnswer: B \r\n \r\n59. Question 9: \r\nWhich type of memory management allows a process to use more memory than \r\nthe amount of physical RAM available? \r\nA. Paging that divides memory into small fixed-size frames \r\nB. Virtual memory that extends physical memory through disk space usage \r\nC. Contiguous allocation that uses adjacent memory blocks for processes \r\nD. Segmentation that divides memory based on logical program structure \r\nAnswer: B \r\n \r\n60. Question 10: \r\nWhat is the main purpose of swapping in memory management systems? \r\nA. To permanently remove inactive processes from main memory space \r\nB. To temporarily move processes between main memory and secondary storage \r\nC. To increase CPU processing speed by reordering instructions \r\nD. To allocate cache memory dynamically during program execution \r\nAnswer: B \r\n \r\n61. Question 11: \r\nIn segmentation, what does each segment in memory represent? \r\nA. A fixed-size block of physical memory used for allocation \r\nB. A logical unit such as code, stack, or data used by a program \r\nC. A single machine instruction executed by the CPU \r\nD. A page frame used in paging memory systems \r\nAnswer: B \r\n \r\n62. Question 12: \r\nWhich of the following statements about static partitioning in memory \r\nmanagement is correct? \r\nA. Memory is divided into equal-sized partitions dynamically at runtime \r\nB. Partitions are created when the system starts and remain fixed in size \r\nC. Partition sizes change automatically depending on process requirements \r\nD. Static partitioning completely eliminates memory fragmentation issues \r\nAnswer: B \r\n \r\n63. Question 13: \r\nWhat is the main drawback of dynamic partitioning in memory allocation? \r\nA. It results in internal fragmentation within allocated partitions \r\nB. It leads to external fragmentation between allocated partitions \r\nC. It lacks flexibility for different process size requirements \r\nD. It reduces CPU performance due to complex memory management \r\nAnswer: B \r\n \r\n64. Question 14: \r\nIn a segmented memory system, what does the logical address consist of? \r\nA. A page number and an offset that determine the physical address \r\nB. A segment number and an offset within that specific memory segment \r\nC. A frame number and an offset calculated by the page table \r\nD. A block number and a displacement within the storage device \r\nAnswer: B \r\n65. Question 15:  \r\nA memory has partitions with sizes (in KB): 16, 53, 22, 37, 62, 44. If a process of size \r\n32 KB requests memory, which partition size (in KB) will be chosen if using the First \r\nFit method? \r\nA. 53 \r\nB. 62 \r\nC. 37 \r\nD. 44 \r\nANSWER: A \r\n \r\n				\N	\N	\N	\N	\N	f
6	test1@gmail.com	Thinh	$2a$10$0mGNMQF3D962MnoiYTeqHOqKOTpQ0o9CWXwWcIyOMJUOV1aK5FW8q	ADMIN		https://res.cloudinary.com/dgny2gq8p/image/upload/v1777299937/ttjobs/avatar/user-6-1777299933658.jpg	\N	\N	\N		\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	f
2	thinh1@gmail.com	\N	$2a$10$R26NtCZyOgN34puV8BqpROE3oGNvFxcOi0Cywpq0pXkIdWCgb4Bz6	CANDIDATE	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	f
3	taikhoan1@gmail.com	\N	$2a$10$gAxEZKx3NPCihAiNYWpas.gRUFyyRUv0iJXgHqw9JKj8ps/FKk5iK	CANDIDATE	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	f
4	taikhoan1@gmail.com	\N	$2a$10$MWXRNcujtzEswRfWNCxJL..SOKpeJACNgbr3B9FSyYlQzoCnl/h32	CANDIDATE	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	f
5	taikhoan123@gmail.com	\N	$2a$10$.8lfAiHyBoiXrPj.y73axOopPrTU1rPmS9D5d2OcxFJKWS0qGDMuS	CANDIDATE	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	f
7	candidate-15250737171500@test.local	test-candidate	dummy-hash	CANDIDATE	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	f
8	candidate-15251172240000@test.local	test-candidate	dummy-hash	CANDIDATE	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	f
9	recruiter-15251176269400@test.local	test-recruiter	dummy-hash	RECRUITER	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	f
10	admin-15251221406700@test.local	test-admin	dummy-hash	ADMIN	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	f
11	candidate-15251244851000@test.local	test-candidate	dummy-hash	CANDIDATE	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	f
12	candidate-15251261106500@test.local	test-candidate	dummy-hash	CANDIDATE	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	f
13	candidate-15251264203000@test.local	test-candidate	dummy-hash	CANDIDATE	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	f
14	recruiter-15251267581100@test.local	test-recruiter	dummy-hash	RECRUITER	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	f
15	candidate-15295734632300@test.local	test-candidate	dummy-hash	CANDIDATE	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	f
16	admin-15296080017300@test.local	test-admin	dummy-hash	ADMIN	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	f
17	candidate-15296099445200@test.local	test-candidate	dummy-hash	CANDIDATE	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	f
18	candidate-15321396647800@test.local	test-candidate	dummy-hash	CANDIDATE	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	f
19	admin-15321826554900@test.local	test-admin	dummy-hash	ADMIN	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	f
20	candidate-15321850280400@test.local	test-candidate	dummy-hash	CANDIDATE	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	f
21	candidate-15954487537500@test.local	test-candidate	dummy-hash	CANDIDATE	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	f
22	admin-15955078502400@test.local	test-admin	dummy-hash	ADMIN	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	f
23	candidate-15955107555100@test.local	test-candidate	dummy-hash	CANDIDATE	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	f
24	candidate-16317930080800@test.local	test-candidate	dummy-hash	CANDIDATE	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	f
25	admin-16318585530000@test.local	test-admin	dummy-hash	ADMIN	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	f
26	candidate-16318615012100@test.local	test-candidate	dummy-hash	CANDIDATE	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	f
27	candidate-16349411162500@test.local	test-candidate	dummy-hash	CANDIDATE	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	f
28	admin-16350052278100@test.local	test-admin	dummy-hash	ADMIN	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	f
29	candidate-16350086426300@test.local	test-candidate	dummy-hash	CANDIDATE	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	f
30	candidate-52993714225100@test.local	test-candidate	dummy-hash	CANDIDATE	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	f
31	admin-52995385073800@test.local	test-admin	dummy-hash	ADMIN	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	f
32	candidate-52995415894800@test.local	test-candidate	dummy-hash	CANDIDATE	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	f
33	candidate-53096766869600@test.local	test-candidate	dummy-hash	CANDIDATE	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	f
34	admin-53097355462000@test.local	test-admin	dummy-hash	ADMIN	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	f
35	candidate-53097395610100@test.local	test-candidate	dummy-hash	CANDIDATE	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	f
36	candidate-58778222314000@test.local	test-candidate	dummy-hash	CANDIDATE	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	f
37	admin-58778813440100@test.local	test-admin	dummy-hash	ADMIN	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	f
38	candidate-58778855498600@test.local	test-candidate	dummy-hash	CANDIDATE	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	f
39	candidate-60345985008000@test.local	test-candidate	dummy-hash	CANDIDATE	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	f
40	admin-60346627353900@test.local	test-admin	dummy-hash	ADMIN	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	f
41	recruiter-60346660168400@test.local	test-recruiter	dummy-hash	RECRUITER	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	f
42	recruiter-60346680754000@test.local	test-recruiter	dummy-hash	RECRUITER	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	f
43	candidate-60346775957700@test.local	test-candidate	dummy-hash	CANDIDATE	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	f
44	recruiter-60346818909700@test.local	test-recruiter	dummy-hash	RECRUITER	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	f
45	recruiter-60346870025700@test.local	test-recruiter	dummy-hash	RECRUITER	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	f
46	candidate-60346877525100@test.local	test-candidate	dummy-hash	CANDIDATE	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	f
47	candidate-60403995167700@test.local	test-candidate	dummy-hash	CANDIDATE	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	f
48	admin-60404496283200@test.local	test-admin	dummy-hash	ADMIN	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	f
49	recruiter-60404576882500@test.local	test-recruiter	dummy-hash	RECRUITER	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	f
50	recruiter-60404605393700@test.local	test-recruiter	dummy-hash	RECRUITER	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	f
51	candidate-60404703944300@test.local	test-candidate	dummy-hash	CANDIDATE	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	f
52	recruiter-60404739227200@test.local	test-recruiter	dummy-hash	RECRUITER	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	f
53	recruiter-60404799678600@test.local	test-recruiter	dummy-hash	RECRUITER	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	f
54	candidate-60404809000400@test.local	test-candidate	dummy-hash	CANDIDATE	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	f
55	candidate-60441318001400@test.local	test-candidate	dummy-hash	CANDIDATE	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	f
56	admin-60443111714700@test.local	test-admin	dummy-hash	ADMIN	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	f
57	recruiter-60443139442300@test.local	test-recruiter	dummy-hash	RECRUITER	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	f
58	recruiter-60443156341400@test.local	test-recruiter	dummy-hash	RECRUITER	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	f
59	candidate-60443316147800@test.local	test-candidate	dummy-hash	CANDIDATE	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	f
60	recruiter-60443390147900@test.local	test-recruiter	dummy-hash	RECRUITER	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	f
61	recruiter-60443444976100@test.local	test-recruiter	dummy-hash	RECRUITER	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	f
62	candidate-60443451155900@test.local	test-candidate	dummy-hash	CANDIDATE	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	f
63	candidate-97022301588400@test.local	test-candidate	dummy-hash	CANDIDATE	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	f
64	admin-97022852804600@test.local	test-admin	dummy-hash	ADMIN	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	f
65	recruiter-97022871616800@test.local	test-recruiter	dummy-hash	RECRUITER	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	f
66	recruiter-97022888118600@test.local	test-recruiter	dummy-hash	RECRUITER	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	f
67	candidate-97022981581700@test.local	test-candidate	dummy-hash	CANDIDATE	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	f
68	recruiter-97022999343100@test.local	test-recruiter	dummy-hash	RECRUITER	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	f
69	recruiter-97023046546500@test.local	test-recruiter	dummy-hash	RECRUITER	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	f
70	candidate-97023054200000@test.local	test-candidate	dummy-hash	CANDIDATE	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	f
71	candidate-97053739304600@test.local	test-candidate	dummy-hash	CANDIDATE	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	f
72	admin-97055210871300@test.local	test-admin	dummy-hash	ADMIN	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	f
73	recruiter-97055226489700@test.local	test-recruiter	dummy-hash	RECRUITER	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	f
74	recruiter-97055238378800@test.local	test-recruiter	dummy-hash	RECRUITER	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	f
75	candidate-97055371797300@test.local	test-candidate	dummy-hash	CANDIDATE	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	f
76	recruiter-97055402599000@test.local	test-recruiter	dummy-hash	RECRUITER	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	f
77	recruiter-97055447578300@test.local	test-recruiter	dummy-hash	RECRUITER	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	f
78	candidate-97055455600200@test.local	test-candidate	dummy-hash	CANDIDATE	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	f
79	candidate-2571687781500@test.local	test-candidate	dummy-hash	CANDIDATE	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	f
80	admin-2572189138200@test.local	test-admin	dummy-hash	ADMIN	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	f
81	recruiter-2572208781700@test.local	test-recruiter	dummy-hash	RECRUITER	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	f
82	recruiter-2572221900300@test.local	test-recruiter	dummy-hash	RECRUITER	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	f
83	candidate-2572276135000@test.local	test-candidate	dummy-hash	CANDIDATE	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	f
84	recruiter-2572289780200@test.local	test-recruiter	dummy-hash	RECRUITER	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	f
85	recruiter-2572321163800@test.local	test-recruiter	dummy-hash	RECRUITER	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	f
86	candidate-2572325245500@test.local	test-candidate	dummy-hash	CANDIDATE	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	f
87	candidate-2596153176600@test.local	test-candidate	dummy-hash	CANDIDATE	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	f
88	admin-2597135500300@test.local	test-admin	dummy-hash	ADMIN	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	f
89	recruiter-2597152279600@test.local	test-recruiter	dummy-hash	RECRUITER	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	f
90	recruiter-2597161955400@test.local	test-recruiter	dummy-hash	RECRUITER	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	f
91	candidate-2597249258100@test.local	test-candidate	dummy-hash	CANDIDATE	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	f
92	recruiter-2597269043100@test.local	test-recruiter	dummy-hash	RECRUITER	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	f
93	recruiter-2597297431300@test.local	test-recruiter	dummy-hash	RECRUITER	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	f
94	candidate-2597302985300@test.local	test-candidate	dummy-hash	CANDIDATE	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	f
95	candidate-4073424611600@test.local	test-candidate	dummy-hash	CANDIDATE	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	f
96	admin-4074006358500@test.local	test-admin	dummy-hash	ADMIN	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	f
97	recruiter-4074039794700@test.local	test-recruiter	dummy-hash	RECRUITER	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	f
98	recruiter-4074057077800@test.local	test-recruiter	dummy-hash	RECRUITER	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	f
99	candidate-4074181623300@test.local	test-candidate	dummy-hash	CANDIDATE	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	f
100	recruiter-4074216395900@test.local	test-recruiter	dummy-hash	RECRUITER	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	f
101	recruiter-4074281990300@test.local	test-recruiter	dummy-hash	RECRUITER	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	f
102	candidate-4074290660900@test.local	test-candidate	dummy-hash	CANDIDATE	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	f
103	candidate-4116399341200@test.local	test-candidate	dummy-hash	CANDIDATE	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	f
104	admin-4116979114500@test.local	test-admin	dummy-hash	ADMIN	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	f
105	recruiter-4117007504700@test.local	test-recruiter	dummy-hash	RECRUITER	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	f
106	recruiter-4117019936300@test.local	test-recruiter	dummy-hash	RECRUITER	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	f
107	candidate-4117092151699@test.local	test-candidate	dummy-hash	CANDIDATE	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	f
108	recruiter-4117117896800@test.local	test-recruiter	dummy-hash	RECRUITER	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	f
109	recruiter-4117171773100@test.local	test-recruiter	dummy-hash	RECRUITER	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	f
110	candidate-4117179307300@test.local	test-candidate	dummy-hash	CANDIDATE	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	f
111	candidate-6065772994900@test.local	test-candidate	dummy-hash	CANDIDATE	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	f
112	admin-6066375745200@test.local	test-admin	dummy-hash	ADMIN	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	f
113	recruiter-6066413082600@test.local	test-recruiter	dummy-hash	RECRUITER	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	f
114	recruiter-6066427540900@test.local	test-recruiter	dummy-hash	RECRUITER	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	f
115	candidate-6066571544200@test.local	test-candidate	dummy-hash	CANDIDATE	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	f
116	recruiter-6066619188200@test.local	test-recruiter	dummy-hash	RECRUITER	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	f
117	recruiter-6066688319700@test.local	test-recruiter	dummy-hash	RECRUITER	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	f
118	candidate-6066696646600@test.local	test-candidate	dummy-hash	CANDIDATE	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	f
119	candidate-11364551382000@test.local	test-candidate	dummy-hash	CANDIDATE	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	f
120	admin-11365325643900@test.local	test-admin	dummy-hash	ADMIN	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	f
121	recruiter-11365350660500@test.local	test-recruiter	dummy-hash	RECRUITER	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	f
122	recruiter-11365362947500@test.local	test-recruiter	dummy-hash	RECRUITER	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	f
123	candidate-11365444136000@test.local	test-candidate	dummy-hash	CANDIDATE	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	f
124	recruiter-11365472097100@test.local	test-recruiter	dummy-hash	RECRUITER	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	f
125	recruiter-11365562511200@test.local	test-recruiter	dummy-hash	RECRUITER	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	f
126	candidate-11365569649100@test.local	test-candidate	dummy-hash	CANDIDATE	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	f
127	candidate-11400008106000@test.local	test-candidate	dummy-hash	CANDIDATE	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	f
128	admin-11401944830300@test.local	test-admin	dummy-hash	ADMIN	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	f
129	recruiter-11401980399500@test.local	test-recruiter	dummy-hash	RECRUITER	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	f
130	recruiter-11401999378500@test.local	test-recruiter	dummy-hash	RECRUITER	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	f
131	candidate-11402198372400@test.local	test-candidate	dummy-hash	CANDIDATE	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	f
132	recruiter-11402231388600@test.local	test-recruiter	dummy-hash	RECRUITER	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	f
133	recruiter-11402303903100@test.local	test-recruiter	dummy-hash	RECRUITER	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	f
134	candidate-11402310818700@test.local	test-candidate	dummy-hash	CANDIDATE	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	f
135	candidate-82995879770300@test.local	test-candidate	dummy-hash	CANDIDATE	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	f
136	admin-82996334541800@test.local	test-admin	dummy-hash	ADMIN	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	f
137	recruiter-82996357801900@test.local	test-recruiter	dummy-hash	RECRUITER	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	f
138	recruiter-82996374995800@test.local	test-recruiter	dummy-hash	RECRUITER	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	f
139	candidate-82996487208200@test.local	test-candidate	dummy-hash	CANDIDATE	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	f
140	recruiter-82996517850500@test.local	test-recruiter	dummy-hash	RECRUITER	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	f
141	recruiter-82996619682600@test.local	test-recruiter	dummy-hash	RECRUITER	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	f
142	candidate-82996627884000@test.local	test-candidate	dummy-hash	CANDIDATE	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	f
143	candidate-139606225412300@test.local	test-candidate	dummy-hash	CANDIDATE	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	f
144	admin-139606851246300@test.local	test-admin	dummy-hash	ADMIN	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	f
145	recruiter-139606874268099@test.local	test-recruiter	dummy-hash	RECRUITER	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	f
146	recruiter-139606887100700@test.local	test-recruiter	dummy-hash	RECRUITER	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	f
147	candidate-139606978300600@test.local	test-candidate	dummy-hash	CANDIDATE	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	f
148	recruiter-139607011960700@test.local	test-recruiter	dummy-hash	RECRUITER	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	f
149	recruiter-139607105473600@test.local	test-recruiter	dummy-hash	RECRUITER	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	f
150	candidate-139607115353700@test.local	test-candidate	dummy-hash	CANDIDATE	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	f
151	candidate-142767420439000@test.local	test-candidate	dummy-hash	CANDIDATE	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	f
152	admin-142768868588500@test.local	test-admin	dummy-hash	ADMIN	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	f
153	recruiter-142768882491200@test.local	test-recruiter	dummy-hash	RECRUITER	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	f
154	recruiter-142768897597500@test.local	test-recruiter	dummy-hash	RECRUITER	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	f
155	candidate-142769023813700@test.local	test-candidate	dummy-hash	CANDIDATE	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	f
156	recruiter-142769062763900@test.local	test-recruiter	dummy-hash	RECRUITER	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	f
157	recruiter-142769315958700@test.local	test-recruiter	dummy-hash	RECRUITER	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	f
158	candidate-142769323327800@test.local	test-candidate	dummy-hash	CANDIDATE	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	f
160	candidate-15423939682800@test.local	test-candidate	dummy-hash	CANDIDATE	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	f
161	admin-15424798324600@test.local	test-admin	dummy-hash	ADMIN	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	f
162	recruiter-15424847316300@test.local	test-recruiter	dummy-hash	RECRUITER	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	f
163	recruiter-15424869348100@test.local	test-recruiter	dummy-hash	RECRUITER	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	f
164	candidate-15424949294300@test.local	test-candidate	dummy-hash	CANDIDATE	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	f
165	recruiter-15424992606800@test.local	test-recruiter	dummy-hash	RECRUITER	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	f
166	recruiter-15425189696000@test.local	test-recruiter	dummy-hash	RECRUITER	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	f
167	candidate-15425197020500@test.local	test-candidate	dummy-hash	CANDIDATE	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	f
168	candidate-15559527444300@test.local	test-candidate	dummy-hash	CANDIDATE	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	f
169	admin-15560448225000@test.local	test-admin	dummy-hash	ADMIN	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	f
170	recruiter-15560483035500@test.local	test-recruiter	dummy-hash	RECRUITER	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	f
171	recruiter-15560496128300@test.local	test-recruiter	dummy-hash	RECRUITER	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	f
172	candidate-15560609959500@test.local	test-candidate	dummy-hash	CANDIDATE	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	f
173	recruiter-15560660348200@test.local	test-recruiter	dummy-hash	RECRUITER	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	f
174	recruiter-15560886059400@test.local	test-recruiter	dummy-hash	RECRUITER	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	f
175	candidate-15560895739900@test.local	test-candidate	dummy-hash	CANDIDATE	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	f
176	nguyenthanhthinh02081@gmail.com	Hoang Dat	$2a$10$BgJqYTQJ1ZuZvywuWKXuVes1XASrWZ38x0IqxoZ3T7Z6jy1ARBgwa	CANDIDATE	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	f
178	candidate-19653721994200@test.local	test-candidate	dummy-hash	CANDIDATE	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	f
179	admin-19655345950800@test.local	test-admin	dummy-hash	ADMIN	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	f
180	recruiter-19655378685300@test.local	test-recruiter	dummy-hash	RECRUITER	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	f
181	recruiter-19655400661600@test.local	test-recruiter	dummy-hash	RECRUITER	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	f
182	candidate-19655513759300@test.local	test-candidate	dummy-hash	CANDIDATE	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	f
183	recruiter-19655560138400@test.local	test-recruiter	dummy-hash	RECRUITER	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	f
184	recruiter-19655753912700@test.local	test-recruiter	dummy-hash	RECRUITER	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	f
185	candidate-19655763072500@test.local	test-candidate	dummy-hash	CANDIDATE	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	f
186	candidate-19743857235800@test.local	test-candidate	dummy-hash	CANDIDATE	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	f
187	admin-19744182409000@test.local	test-admin	dummy-hash	ADMIN	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	f
188	recruiter-19744212510600@test.local	test-recruiter	dummy-hash	RECRUITER	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	f
189	recruiter-19744222439700@test.local	test-recruiter	dummy-hash	RECRUITER	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	f
190	candidate-19744374836600@test.local	test-candidate	dummy-hash	CANDIDATE	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	f
191	recruiter-19744404292700@test.local	test-recruiter	dummy-hash	RECRUITER	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	f
192	recruiter-19744523371300@test.local	test-recruiter	dummy-hash	RECRUITER	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	f
193	candidate-19744530258800@test.local	test-candidate	dummy-hash	CANDIDATE	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	f
194	candidate-19803598083900@test.local	test-candidate	dummy-hash	CANDIDATE	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	f
195	admin-19804089283100@test.local	test-admin	dummy-hash	ADMIN	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	f
196	recruiter-19804127172800@test.local	test-recruiter	dummy-hash	RECRUITER	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	f
197	recruiter-19804146557700@test.local	test-recruiter	dummy-hash	RECRUITER	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	f
198	candidate-19804225931900@test.local	test-candidate	dummy-hash	CANDIDATE	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	f
199	recruiter-19804257365000@test.local	test-recruiter	dummy-hash	RECRUITER	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	f
200	recruiter-19804370222200@test.local	test-recruiter	dummy-hash	RECRUITER	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	f
201	candidate-19804379476300@test.local	test-candidate	dummy-hash	CANDIDATE	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	f
202	candidate-20286205452300@test.local	test-candidate	dummy-hash	CANDIDATE	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	f
203	admin-20286615987900@test.local	test-admin	dummy-hash	ADMIN	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	f
204	recruiter-20286655300700@test.local	test-recruiter	dummy-hash	RECRUITER	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	f
205	recruiter-20286677749200@test.local	test-recruiter	dummy-hash	RECRUITER	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	f
206	candidate-20286792911500@test.local	test-candidate	dummy-hash	CANDIDATE	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	f
207	recruiter-20286852657800@test.local	test-recruiter	dummy-hash	RECRUITER	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	f
208	recruiter-20286987242700@test.local	test-recruiter	dummy-hash	RECRUITER	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	f
209	candidate-20286996751800@test.local	test-candidate	dummy-hash	CANDIDATE	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	f
210	candidate-32750365432800@test.local	test-candidate	dummy-hash	CANDIDATE	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	f
211	admin-32751750603100@test.local	test-admin	dummy-hash	ADMIN	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	f
212	recruiter-32751781763200@test.local	test-recruiter	dummy-hash	RECRUITER	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	f
213	recruiter-32751804537500@test.local	test-recruiter	dummy-hash	RECRUITER	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	f
214	candidate-32751952222600@test.local	test-candidate	dummy-hash	CANDIDATE	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	f
215	recruiter-32752028750800@test.local	test-recruiter	dummy-hash	RECRUITER	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	f
216	recruiter-32752339220000@test.local	test-recruiter	dummy-hash	RECRUITER	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	f
217	candidate-32752347691100@test.local	test-candidate	dummy-hash	CANDIDATE	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	f
218	candidate-48022380662800@test.local	test-candidate	dummy-hash	CANDIDATE	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	f
219	admin-48023205876600@test.local	test-admin	dummy-hash	ADMIN	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	f
220	recruiter-48023231272800@test.local	test-recruiter	dummy-hash	RECRUITER	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	f
221	recruiter-48023256424000@test.local	test-recruiter	dummy-hash	RECRUITER	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	f
222	candidate-48023375310300@test.local	test-candidate	dummy-hash	CANDIDATE	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	f
223	recruiter-48023414074300@test.local	test-recruiter	dummy-hash	RECRUITER	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	f
224	recruiter-48023597543000@test.local	test-recruiter	dummy-hash	RECRUITER	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	f
225	candidate-48023606424900@test.local	test-candidate	dummy-hash	CANDIDATE	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	f
226	candidate-48265440563000@test.local	test-candidate	dummy-hash	CANDIDATE	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	f
227	admin-48265983039200@test.local	test-admin	dummy-hash	ADMIN	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	f
228	recruiter-48266014092600@test.local	test-recruiter	dummy-hash	RECRUITER	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	f
229	recruiter-48266026309900@test.local	test-recruiter	dummy-hash	RECRUITER	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	f
230	candidate-48266129415300@test.local	test-candidate	dummy-hash	CANDIDATE	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	f
231	recruiter-48266172860600@test.local	test-recruiter	dummy-hash	RECRUITER	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	f
232	recruiter-48266419970900@test.local	test-recruiter	dummy-hash	RECRUITER	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	f
233	candidate-48266431764400@test.local	test-candidate	dummy-hash	CANDIDATE	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	f
235	candidate-118132539975800@test.local	test-candidate	dummy-hash	CANDIDATE	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	f
236	admin-118133443380100@test.local	test-admin	dummy-hash	ADMIN	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	f
237	recruiter-118133493107600@test.local	test-recruiter	dummy-hash	RECRUITER	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	f
238	recruiter-118133514450300@test.local	test-recruiter	dummy-hash	RECRUITER	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	f
239	candidate-118133655874400@test.local	test-candidate	dummy-hash	CANDIDATE	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	f
240	recruiter-118133705528500@test.local	test-recruiter	dummy-hash	RECRUITER	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	f
241	recruiter-118133970543900@test.local	test-recruiter	dummy-hash	RECRUITER	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	f
242	candidate-118133978033700@test.local	test-candidate	dummy-hash	CANDIDATE	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	f
243	candidate-118269471714600@test.local	test-candidate	dummy-hash	CANDIDATE	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	f
244	admin-118270023305800@test.local	test-admin	dummy-hash	ADMIN	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	f
245	recruiter-118270044239600@test.local	test-recruiter	dummy-hash	RECRUITER	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	f
246	recruiter-118270054369700@test.local	test-recruiter	dummy-hash	RECRUITER	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	f
247	candidate-118270135064400@test.local	test-candidate	dummy-hash	CANDIDATE	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	f
248	recruiter-118270171081400@test.local	test-recruiter	dummy-hash	RECRUITER	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	f
249	recruiter-118270329300200@test.local	test-recruiter	dummy-hash	RECRUITER	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	f
250	candidate-118270337439000@test.local	test-candidate	dummy-hash	CANDIDATE	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	f
159	nguyenthanhthinh0208@gmail.com	Hoang Dat	$2a$10$rK7yyN6atAhnp9rxeFVjfuAzDi9FCcl6WaJzdgPSBgU87XNJ.K03m	CANDIDATE	\N	\N	\N	\N	\N	\N	\N	\nMô phỏng AGV tự hành thăm dò không gian chưa biết bằng ROS2 SLAM và Navigation\n\nPART 1. INTRODUCTION\nSlide Title: Project Overview\nTopic: Simulation of an Autonomous AGV Exploring an Unknown Environment Using ROS2 SLAM and Navigation\nAGV (Automated Guided Vehicle): Smart autonomous vehicle used in industry, logistics, and rescue operations.\nProblem: AGVs often work in unknown or dynamic environments → need the ability to explore and map autonomously.\nObjectives:\nSimulate an AGV capable of self-localization, obstacle avoidance, mapping, and exploration.\nApply ROS2, SLAM Toolbox, and Navigation2 within the Gazebo simulation environment.\nPractical significance:\nUseful for rescue robots, exploration missions, warehouse inspection, and smart factories.\n\nPART 2. THEORETICAL BACKGROUND & TECHNOLOGY\nSlide Title: Technologies Used\nROS2 (Robot Operating System 2)\nOpen-source framework for robot control and communication.\nManages nodes, topics, services, and actions.\nProvides easy integration between simulation and real robots.\nGazebo Simulation\n3D physics-based simulation environment.\nUsed to model worlds, sensors, and robot behavior.\n\nSlide Title: SLAM and Navigation\nSLAM (Simultaneous Localization and Mapping)\nEnables a robot to build a map and localize itself simultaneously.\nUses LiDAR sensor data for mapping.\nImplemented with slam_toolbox.\nNavigation2 (Nav2)\nProvides path planning, obstacle avoidance, and safe navigation.\nMain components:\nPlanner Server\nController Server\nMap Server\nRecovery Server\nExplore Lite / Frontier Exploration\nAutomatically detects unexplored (frontier) regions and sends navigation goals.\nAllows the AGV to autonomously explore the entire environment.\n(💡 Tip: add a diagram showing data flow: LiDAR → SLAM → Map → Nav2 → Explore → Robot)\nPART 3. SYSTEM DESIGN & IMPLEMENTATION\nSlide Title: Simulation Architecture\nSimulation environment: Gazebo\nWorld designed as a maze or warehouse with obstacles.\nAGV robot model:\nTwo driving wheels + one caster wheel.\nEquipped with 360° LiDAR sensor.\nUses diff_drive_controller plugin for motion control.\nDescribed in .urdf or .sdf format.\nSlide Title: System Workflow\nSLAM Toolbox builds the map in real time.\nNavigation2 plans and follows safe paths.\nExplore Lite detects unknown areas and generates exploration goals.\nRViz2 visualizes map, robot pose, and trajectory.\nROS2 packages used:\nslam_toolbox, nav2_bringup, explore_lite, gazebo_ros_pkgs.\n\nSlide Title: Implementation Steps\nLaunch Gazebo with the AGV model.\nStart SLAM to begin mapping.\nLaunch Navigation2 for path planning.\nRun Explore Lite for autonomous exploration.\nMonitor progress in RViz2.\nSave the generated map using map_saver_cli.\n\nPART 4. RESULTS AND EVALUATION\nSlide Title: Simulation Results\nAGV successfully navigated and explored the entire environment autonomously.\nReal-time map generation observed during operation.\nRobot avoided obstacles effectively and covered unknown regions.\nFinal map saved for reuse.\nRViz2 visualization clearly shows map, trajectory, and explored areas.\n(💡 Include two images: one from Gazebo, one from RViz2 showing the map)\n\nSlide Title: Evaluation and Discussion\nStrengths:\nStable performance and flexible system architecture.\nFully open-source ROS2 implementation.\nRealistic simulation results.\nLimitations:\nCurrently limited to simulation (no real hardware).\nExploration time depends on the frontier algorithm.\nFuture improvements:\nIntegrate RGB-D camera for Visual SLAM.\nImplement database storage for multiple maps.\nExtend to multi-AGV exploration.\n\nSlide Title: Conclusion\nThe project successfully simulated:\nAn AGV autonomously exploring an unknown environment.\nIntegration of ROS2 SLAM Toolbox and Navigation2.\nStable, accurate mapping and navigation.\nResults:\nDemonstrated strong potential for real-world industrial, rescue, and logistics applications.\nFuture work:\nDeploy on real hardware.\nAdd camera-based mapping, multi-robot coordination, and map database management.\n	\N	\N	\N	\N	\N	\N	\N	\N	f
251	candidate-5677148380400@test.local	test-candidate	dummy-hash	CANDIDATE	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	f
252	admin-5678062561900@test.local	test-admin	dummy-hash	ADMIN	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	f
253	recruiter-5678090999600@test.local	test-recruiter	dummy-hash	RECRUITER	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	f
254	recruiter-5678115485800@test.local	test-recruiter	dummy-hash	RECRUITER	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	f
255	candidate-5678248753000@test.local	test-candidate	dummy-hash	CANDIDATE	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	f
256	recruiter-5678285438900@test.local	test-recruiter	dummy-hash	RECRUITER	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	f
257	recruiter-5678584284800@test.local	test-recruiter	dummy-hash	RECRUITER	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	f
258	candidate-5678590928100@test.local	test-candidate	dummy-hash	CANDIDATE	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	f
259	candidate-5830063442200@test.local	test-candidate	dummy-hash	CANDIDATE	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	f
260	admin-5830488039900@test.local	test-admin	dummy-hash	ADMIN	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	f
261	recruiter-5830525406500@test.local	test-recruiter	dummy-hash	RECRUITER	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	f
262	recruiter-5830544416100@test.local	test-recruiter	dummy-hash	RECRUITER	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	f
263	candidate-5830659295700@test.local	test-candidate	dummy-hash	CANDIDATE	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	f
264	recruiter-5830703628800@test.local	test-recruiter	dummy-hash	RECRUITER	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	f
265	recruiter-5830951251300@test.local	test-recruiter	dummy-hash	RECRUITER	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	f
266	candidate-5830962094800@test.local	test-candidate	dummy-hash	CANDIDATE	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	f
267	candidate-6098755502600@test.local	test-candidate	dummy-hash	CANDIDATE	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	f
268	admin-6099665497300@test.local	test-admin	dummy-hash	ADMIN	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	f
269	recruiter-6099919095900@test.local	test-recruiter	dummy-hash	RECRUITER	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	f
270	recruiter-6100054161000@test.local	test-recruiter	dummy-hash	RECRUITER	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	f
271	candidate-6100281097900@test.local	test-candidate	dummy-hash	CANDIDATE	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	f
272	recruiter-6100338166800@test.local	test-recruiter	dummy-hash	RECRUITER	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	f
273	recruiter-6100630569500@test.local	test-recruiter	dummy-hash	RECRUITER	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	f
274	candidate-6100641445200@test.local	test-candidate	dummy-hash	CANDIDATE	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	f
275	candidate-9523685809000@test.local	test-candidate	dummy-hash	CANDIDATE	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	f
276	admin-9524273898200@test.local	test-admin	dummy-hash	ADMIN	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	f
277	recruiter-9524322498300@test.local	test-recruiter	dummy-hash	RECRUITER	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	f
278	recruiter-9524341654000@test.local	test-recruiter	dummy-hash	RECRUITER	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	f
279	candidate-9524488566900@test.local	test-candidate	dummy-hash	CANDIDATE	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	f
280	recruiter-9524527914500@test.local	test-recruiter	dummy-hash	RECRUITER	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	f
281	recruiter-9524924953500@test.local	test-recruiter	dummy-hash	RECRUITER	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	f
282	candidate-9524959668500@test.local	test-candidate	dummy-hash	CANDIDATE	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	f
283	candidate-12290310772700@test.local	test-candidate	dummy-hash	CANDIDATE	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	f
284	admin-12290850052200@test.local	test-admin	dummy-hash	ADMIN	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	f
285	recruiter-12290884830000@test.local	test-recruiter	dummy-hash	RECRUITER	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	f
286	recruiter-12290898788100@test.local	test-recruiter	dummy-hash	RECRUITER	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	f
287	candidate-12291011049200@test.local	test-candidate	dummy-hash	CANDIDATE	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	f
288	recruiter-12291055700700@test.local	test-recruiter	dummy-hash	RECRUITER	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	f
289	recruiter-12291245611500@test.local	test-recruiter	dummy-hash	RECRUITER	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	f
290	candidate-12291254856200@test.local	test-candidate	dummy-hash	CANDIDATE	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	f
291	candidate-29619174874600@test.local	test-candidate	dummy-hash	CANDIDATE	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	f
292	admin-29619872264200@test.local	test-admin	dummy-hash	ADMIN	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	f
293	recruiter-29619899659300@test.local	test-recruiter	dummy-hash	RECRUITER	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	f
294	recruiter-29619918833100@test.local	test-recruiter	dummy-hash	RECRUITER	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	f
295	candidate-29620032140200@test.local	test-candidate	dummy-hash	CANDIDATE	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	f
296	recruiter-29620062505800@test.local	test-recruiter	dummy-hash	RECRUITER	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	f
297	recruiter-29620218949000@test.local	test-recruiter	dummy-hash	RECRUITER	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	f
298	candidate-29620234472100@test.local	test-candidate	dummy-hash	CANDIDATE	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	f
299	candidate-29759001003400@test.local	test-candidate	dummy-hash	CANDIDATE	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	f
300	admin-29759638795800@test.local	test-admin	dummy-hash	ADMIN	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	f
301	recruiter-29759686376200@test.local	test-recruiter	dummy-hash	RECRUITER	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	f
302	recruiter-29759704339600@test.local	test-recruiter	dummy-hash	RECRUITER	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	f
303	candidate-29759827537700@test.local	test-candidate	dummy-hash	CANDIDATE	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	f
304	recruiter-29759884337800@test.local	test-recruiter	dummy-hash	RECRUITER	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	f
305	recruiter-29760112803300@test.local	test-recruiter	dummy-hash	RECRUITER	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	f
306	candidate-29760124497100@test.local	test-candidate	dummy-hash	CANDIDATE	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	f
307	candidate-29867626425700@test.local	test-candidate	dummy-hash	CANDIDATE	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	f
308	admin-29868702176600@test.local	test-admin	dummy-hash	ADMIN	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	f
309	recruiter-29868741166200@test.local	test-recruiter	dummy-hash	RECRUITER	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	f
310	recruiter-29868756947500@test.local	test-recruiter	dummy-hash	RECRUITER	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	f
311	candidate-29868944162900@test.local	test-candidate	dummy-hash	CANDIDATE	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	f
312	recruiter-29869029829000@test.local	test-recruiter	dummy-hash	RECRUITER	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	f
313	recruiter-29869243579600@test.local	test-recruiter	dummy-hash	RECRUITER	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	f
314	candidate-29869252535800@test.local	test-candidate	dummy-hash	CANDIDATE	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	f
315	candidate-30847403784400@test.local	test-candidate	dummy-hash	CANDIDATE	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	f
316	admin-30848339119400@test.local	test-admin	dummy-hash	ADMIN	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	f
317	recruiter-30848385175600@test.local	test-recruiter	dummy-hash	RECRUITER	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	f
318	recruiter-30848403491700@test.local	test-recruiter	dummy-hash	RECRUITER	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	f
319	candidate-30848544531600@test.local	test-candidate	dummy-hash	CANDIDATE	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	f
320	recruiter-30848610095400@test.local	test-recruiter	dummy-hash	RECRUITER	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	f
321	recruiter-30848901631200@test.local	test-recruiter	dummy-hash	RECRUITER	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	f
322	candidate-30848913947600@test.local	test-candidate	dummy-hash	CANDIDATE	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	f
323	candidate-31525243705700@test.local	test-candidate	dummy-hash	CANDIDATE	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	f
324	admin-31526170394800@test.local	test-admin	dummy-hash	ADMIN	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	f
325	recruiter-31526236604100@test.local	test-recruiter	dummy-hash	RECRUITER	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	f
326	recruiter-31526259471000@test.local	test-recruiter	dummy-hash	RECRUITER	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	f
327	candidate-31526425249600@test.local	test-candidate	dummy-hash	CANDIDATE	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	f
328	recruiter-31526462133400@test.local	test-recruiter	dummy-hash	RECRUITER	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	f
329	recruiter-31526853603500@test.local	test-recruiter	dummy-hash	RECRUITER	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	f
330	candidate-31526862416700@test.local	test-candidate	dummy-hash	CANDIDATE	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	f
331	candidate-41629107459400@test.local	test-candidate	dummy-hash	CANDIDATE	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	f
332	admin-41629436168700@test.local	test-admin	dummy-hash	ADMIN	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	f
333	recruiter-41629473388200@test.local	test-recruiter	dummy-hash	RECRUITER	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	f
334	recruiter-41629488640500@test.local	test-recruiter	dummy-hash	RECRUITER	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	f
335	candidate-41629573494000@test.local	test-candidate	dummy-hash	CANDIDATE	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	f
336	recruiter-41629620847400@test.local	test-recruiter	dummy-hash	RECRUITER	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	f
337	recruiter-41629783555400@test.local	test-recruiter	dummy-hash	RECRUITER	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	f
338	candidate-41629791165300@test.local	test-candidate	dummy-hash	CANDIDATE	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	f
339	candidate-41922386058000@test.local	test-candidate	dummy-hash	CANDIDATE	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	f
340	admin-41923191334300@test.local	test-admin	dummy-hash	ADMIN	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	f
341	recruiter-41923223399000@test.local	test-recruiter	dummy-hash	RECRUITER	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	f
342	recruiter-41923241057500@test.local	test-recruiter	dummy-hash	RECRUITER	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	f
343	candidate-41923377069600@test.local	test-candidate	dummy-hash	CANDIDATE	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	f
344	recruiter-41923481865800@test.local	test-recruiter	dummy-hash	RECRUITER	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	f
345	recruiter-41923693889400@test.local	test-recruiter	dummy-hash	RECRUITER	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	f
346	candidate-41923702741800@test.local	test-candidate	dummy-hash	CANDIDATE	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	f
347	candidate-120930065801700@test.local	test-candidate	dummy-hash	CANDIDATE	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	f
348	admin-120930617033000@test.local	test-admin	dummy-hash	ADMIN	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	f
349	recruiter-120930653044800@test.local	test-recruiter	dummy-hash	RECRUITER	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	f
350	recruiter-120930684755100@test.local	test-recruiter	dummy-hash	RECRUITER	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	f
351	candidate-120930781116800@test.local	test-candidate	dummy-hash	CANDIDATE	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	f
352	recruiter-120930812574100@test.local	test-recruiter	dummy-hash	RECRUITER	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	f
353	recruiter-120931001521800@test.local	test-recruiter	dummy-hash	RECRUITER	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	f
354	candidate-120931008660200@test.local	test-candidate	dummy-hash	CANDIDATE	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	f
355	candidate-123125848131100@test.local	test-candidate	dummy-hash	CANDIDATE	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	f
356	admin-123126320933200@test.local	test-admin	dummy-hash	ADMIN	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	f
357	recruiter-123126357814200@test.local	test-recruiter	dummy-hash	RECRUITER	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	f
358	recruiter-123126369183200@test.local	test-recruiter	dummy-hash	RECRUITER	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	f
359	candidate-123126456408500@test.local	test-candidate	dummy-hash	CANDIDATE	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	f
360	recruiter-123126494689800@test.local	test-recruiter	dummy-hash	RECRUITER	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	f
361	recruiter-123126655476600@test.local	test-recruiter	dummy-hash	RECRUITER	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	f
362	candidate-123126661164500@test.local	test-candidate	dummy-hash	CANDIDATE	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	f
363	candidate-130742650174100@test.local	test-candidate	dummy-hash	CANDIDATE	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	f
364	admin-130744139365500@test.local	test-admin	dummy-hash	ADMIN	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	f
365	recruiter-130744224039300@test.local	test-recruiter	dummy-hash	RECRUITER	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	f
366	recruiter-130744251207600@test.local	test-recruiter	dummy-hash	RECRUITER	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	f
367	candidate-130744491010900@test.local	test-candidate	dummy-hash	CANDIDATE	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	f
368	recruiter-130744540024300@test.local	test-recruiter	dummy-hash	RECRUITER	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	f
369	recruiter-130744929387800@test.local	test-recruiter	dummy-hash	RECRUITER	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	f
370	candidate-130744936927000@test.local	test-candidate	dummy-hash	CANDIDATE	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	f
234	nguyenthanhthinh020811@gmail.com	ga2	$2a$10$5rmvxJGPjXlFMYnZcq4GieyoyY/I0.J9IJoE9kTD7MDjcpIbRNQDi	CANDIDATE	\N	\N	\N	\N	\N	\N	\N	CHAPTER 1 \r\n1. Question 1: \r\nWhat is an operating system?   \r\nA. A collection of hardware devices that make up a computer system   \r\nB. Software that manages both computer hardware and software resources \r\nC. A programming language used for writing code and developing applications   \r\nD. An internet browser that allows users to access online websites and services \r\nAnswer: B \r\n \r\n2. Question 2: \r\nWhich of the following is not a function of an operating system? \r\nA. Memory management \r\nB. Process management \r\nC. Hardware manufacturing \r\nD. File management \r\nAnswer: C \r\n \r\n3. Question 3: \r\nWhich of these operating systems is designed mainly for smartphones? \r\nA. Windows \r\nB. Linux \r\nC. Android \r\nD. UNIX \r\nAnswer: C \r\n \r\n4. Question 4: \r\nThe part of an operating system that directly interacts with the hardware is called: \r\nA. User interface \r\nB. Kernel \r\nC. Compiler \r\nD. File system \r\nAnswer: B \r\n \r\n5. Question 5: \r\nWhat is the main purpose of an operating system? \r\nA. To provide an environment that allows users to execute and manage programs \r\nB. To design and develop computer hardware components used in the system   \r\nC. To connect computers and manage communication over the internet network   \r\nD. To compile source code and translate it into executable programming languages \r\nAnswer: A \r\n \r\n6. Question 6: \r\nWhich of the following is an example of a multi-user operating system? \r\nA. MS-DOS \r\nB. Windows 95 \r\nC. UNIX \r\nD. Android \r\nAnswer: C \r\n \r\n7. Question 7: \r\nWhich operating system is open-source and widely used for servers? \r\nA. macOS \r\nB. Windows \r\nC. Linux \r\nD. iOS \r\nAnswer: C \r\n \r\n8. Question 8: \r\nWhat does “booting” refer to in an operating system? \r\nA. Shutting down the computer \r\nB. Starting up the computer & loading the OS \r\nC. Installing software  \r\nD. Formatting the hard drive \r\nAnswer: B \r\n \r\n9. Question 9: \r\nThe user interface that uses icons and windows is called __________. \r\nA. Command-Line Interface (CLI) \r\nB. Graphical User Interface (GUI) \r\nC. Batch Interface \r\nD. Text Interface \r\nAnswer: B \r\n \r\n10. Question 10: \r\nWhich of the following is not a type of operating system? \r\nA. Real-time OS \r\nB. Batch OS \r\nC. Time-sharing OS \r\nD. Compiler OS \r\nAnswer: D \r\n \r\n11. Question 11: \r\nWhich of the following best describes a thread? \r\nA. A complete and independent program executed by the system   \r\nB. The smallest unit of CPU scheduling within a process   \r\nC. A regular file stored and managed in the operating system   \r\nD. A hardware interrupt signal generated by computer devices  \r\nAnswer: B \r\n \r\n12. Question 12: \r\nWhat are system resources in an operating system? \r\nA. Only memory and CPU used by the system   \r\nB. Hardware and software components managed by the OS   \r\nC. Network devices and related connections controlled by the OS   \r\nD. Application programs that interact with system resources   \r\nAnswer: B \r\n \r\n13. Question 13: \r\nWhich of the following is not considered a system resource? \r\nA. CPU time \r\nB. Memory \r\nC. Files \r\nD. Web browser \r\nAnswer: D \r\n \r\n14. Question 14: \r\nThe process control block (PCB) contains which of the following types of \r\ninformation?   \r\nA. The file directory structure and paths used by the operating system   \r\nB. A process’s current state, program counter, CPU registers, and related details   \r\nC. The list of device drivers installed and managed by the operating system   \r\nD. The network configuration and communication parameters of the computer \r\nAnswer: B \r\n \r\n15. Question 15: \r\nWhat is a system call? \r\nA. A request made by a process to the operating system for a service \r\nB. A direct hardware interrupt \r\nC. A programming language function \r\nD. A communication between two processes \r\nAnswer: A \r\n \r\n16. Question 16: \r\nWhich of the following best describes the relationship between a process and its \r\nthreads? \r\nA. Threads share the same memory and system resources of a single process   \r\nB. Each thread operates independently inside its own process environment   \r\nC. Threads are unable to share any data or memory between executions   \r\nD. A process may contain only one thread that performs all operations  \r\nAnswer: A \r\n \r\n17. Question 17: \r\nWhat happens during a context switch? \r\nA. The CPU begins executing a different instruction from the same program   \r\nB. The OS saves the current state of one process and loads another process to run   \r\nC. The computer system performs a restart operation to refresh all running tasks   \r\nD. A user logs out of the current session and ends all active programs  \r\nAnswer: B \r\n \r\n18. Question 18: \r\nWhich part of the operating system executes privileged instructions? \r\nA. User mode \r\nB. Kernel mode \r\nC. Application mode \r\nD. I/O mode \r\nAnswer: B \r\n \r\n19. Question 19: \r\nHow does a process usually request access to a hardware device in an operating \r\nsystem? \r\nA. Direct access to the hardware without any mediation   \r\nB. A system call that is handled and managed by the kernel   \r\nC. A user-level function executed within the application program   \r\nD. The file manager that organizes files and device resources  \r\nAnswer: B \r\n \r\n20. Question 20: \r\nWhich type of operating system executes a batch of jobs without user interaction? \r\nA. Real-time OS \r\nB. Batch OS \r\nC. Time-sharing OS \r\nD. Distributed OS \r\nAnswer: B \r\n \r\n21. Question 21: \r\nIn which type of operating system do multiple users share system resources \r\nsimultaneously? \r\nA. Single-user OS \r\nB. Time-sharing OS \r\nC. Batch OS \r\nD. Real-time OS \r\nAnswer: B \r\n \r\n22. Question 22: \r\nWhich operating system type is used when tasks must be completed within strict \r\ntime limits? \r\nA. Real-time OS \r\nB. Distributed OS \r\nC. Batch OS \r\nD. Network OS \r\nAnswer: A \r\n \r\n23. Question 23: \r\nA distributed operating system is mainly designed to: \r\nA. Manage one single computer system \r\nB. Connect & manage multiple computers as one system \r\nC. Run only on mobile devices \r\nD. Execute one job at a time \r\nAnswer: B \r\n \r\n24. Question 24: \r\nWhich of the following is an example of a network operating system? \r\nA. Windows Server \r\nB. Android \r\nC. macOS \r\nD. MS-DOS \r\nAnswer: A \r\n \r\n25. Question 25: \r\nWhich of the following is a primary function of an operating system? \r\nA. Compiling source code \r\nB. Managing hardware & software resources \r\nC. Designing hardware circuits \r\nD. Creating computer networks \r\nAnswer: B \r\n \r\n26. Question 26: \r\nWhich operating system function keeps track of where programs and data are \r\nstored in memory? \r\nA. File management \r\nB. Memory management \r\nC. Process scheduling \r\nD. Device management \r\nAnswer: B \r\n \r\n27. Question 27: \r\nThe operating system function responsible for controlling input and output devices \r\nis called: \r\nA. Device management \r\nB. File management \r\nC. Network management \r\nD. Process management \r\nAnswer: A \r\n \r\n28. Question 28: \r\nWhich OS function is responsible for allocating the CPU to various processes? \r\nA. Memory management \r\nB. Process scheduling \r\nC. Security management \r\nD. File management \r\nAnswer: B \r\n \r\n29. Question 29: \r\nWhat is the main goal of file management in an operating system? \r\nA. To manage file creation, deletion, & access \r\nB. To install applications \r\nC. To monitor CPU temperature \r\nD. To control peripheral devices \r\nAnswer: A \r\n \r\nCHAPTER 2 \r\n30. Question 1: \r\nWhich scheduling algorithm gives the CPU to the process that arrives first? \r\nA. Shortest Job Next (SJN) \r\nB. First-Come, First-Served (FCFS) \r\nC. Round Robin (RR) \r\nD. Priority Scheduling \r\nAnswer: B \r\n \r\n31. Question 2: \r\nWhich scheduling algorithm assigns a fixed time unit per process in a cyclic \r\norder? \r\nA. FCFS \r\nB. SJF \r\nC. Round Robin (RR) \r\nD. Priority Scheduling \r\nAnswer: C \r\n \r\n32. Question 3: \r\nIn Shortest Job First (SJF) scheduling, which process is selected next? \r\nA. The one with the highest priority \r\nB. The one with the shortest CPU burst time \r\nC. The one that arrived first \r\nD. The one with the longest waiting time \r\nAnswer: B \r\n \r\n33. Question 4: \r\nIn Priority Scheduling, if two processes have the same priority, how is the tie \r\nusually broken? \r\nA. Randomly  \r\nB. Based on process ID \r\nC. By arrival time (FCFS) \r\nD. By memory usage \r\nAnswer: C \r\n \r\n34. Question 5: \r\nWhich of the following scheduling algorithms can cause starvation if not properly \r\nmanaged? \r\nA. FCFS (system term) \r\nB. Round Robin \r\nC. Priority Scheduling \r\nD. Shortest Remaining Time First (SRTF) \r\nAnswer: C \r\n \r\n35. Question 6: \r\nWhat is a process in an operating system? \r\nA. A program that is being executed \r\nB. A system file in storage \r\nC. A hardware component \r\nD. A type of system call \r\nAnswer: A \r\n \r\n36. Question 7: \r\nWhich of the following is not a valid process state? \r\nA. New \r\nB. Running \r\nC. Waiting \r\nD. Compiling \r\nAnswer: D \r\n \r\n37. Question 8: \r\nThe structure that stores all information about a process is called: \r\nA. Process Control Block (PCB) \r\nB. Process Table \r\nC. Stack Pointer \r\nD. Ready Queue \r\nAnswer: A \r\n \r\n38. Question 9: \r\nDuring a context switch, what action is performed by the operating system?   \r\nA. It compiles user programs into machine-level instructions for execution   \r\nB. It saves the current state of one process and loads another process to run   \r\nC. It removes inactive or old processes completely from the system memory   \r\nD. It allocates additional disk space required for process storage operations \r\nAnswer: B \r\n \r\n39. Question 10: \r\nIn a multiprogramming system, processes waiting to use the CPU are kept in: \r\nA. Job queue \r\nB. Ready queue \r\nC. Device queue \r\nD. File queue \r\nAnswer: B \r\n \r\n40. Question 11: \r\nWhich of the following is true about process scheduling? \r\nA. It determines which process will use the CPU next \r\nB. It is handled by the compiler \r\nC. It only occurs in single-user systems \r\nD. It is unrelated to CPU time management \r\nAnswer: A \r\n \r\n41. Question 12: \r\nThe process state changes from Running to Waiting when: \r\nA. The process completes execution \r\nB. The process requests an I/O operation \r\nC. A higher-priority process arrives \r\nD. The CPU fails (system term) \r\nAnswer: B \r\n \r\n42. Question 13: \r\nWhich of the following is not a type of process scheduler? \r\nA. Long-term scheduler \r\nB. Medium-term scheduler \r\nC. Short-term scheduler \r\nD. Hardware scheduler \r\nAnswer: D \r\n \r\n43. Question 14: \r\nWhen a child process is created using a system call such as fork(), what happens?   \r\nA. It shares the same memory and address space with its parent process entirely   \r\nB. It is almost identical to the parent process except for having a unique PID value   \r\nC. It terminates or destroys the parent process immediately after its creation   \r\nD. It runs only in kernel mode and performs no user-level operations   \r\nAnswer: B \r\n \r\n44. Question 15: \r\nWhat is the purpose of process synchronization? \r\nA. To manage the speed of the CPU \r\nB. To ensure orderly execution when processes share resources \r\nC. To increase process priority (system term) \r\nD. To convert processes into threads \r\nAnswer: B \r\n \r\n45. Question 16: \r\nIn the Shortest Remaining Time First (SRTF) scheduling algorithm, what happens \r\nif a new process arrives with a shorter burst time than the one currently running?   \r\nA. The CPU continues executing the currently running process until it finishes \r\ncompletely   \r\nB. The CPU suspends the current process and switches to execute the new shorter \r\nprocess   \r\nC. The CPU executes both processes simultaneously using shared time and \r\nresources   \r\nD. The CPU ignores the new process until the current process has completed \r\nexecution   \r\nAnswer: B \r\n \r\n46. Question 17: \r\nUsing Priority (non-preemptive) algorithm, find the order of execution for the \r\nfollowing processes with the given data in the order Process : Burst Time : \r\nPriority (0 is the highest priority) respectively A : 4 : 4; B : 1 : 1; C : 6 : 3; D : 4 : \r\n6. \r\nA. B, C, D, A \r\nB. B, C, A, D \r\nC. B, A, C, D \r\nD. B, D, A, C \r\nANSWER: B \r\n \r\n47. Question 18: \r\nUsing RR (Round-Robin) algorithm with time quantum: 3, find the average \r\nwaiting time for the following processes with the given data in the order Process \r\n: Arrival Time : Burst Time respectively A : 8 : 3; B : 0 : 5; C : 2 : 4. \r\nA. 2.333 \r\nB. 1.333 \r\nC. 5.333 \r\nD. 4.333 \r\nANSWER: A \r\n \r\n48. Question 19: \r\nUsing SJF (non-preemptive) algorithm, find the order of execution for the \r\nfollowing processes with the given data in the order Process : Arrival Time : \r\nBurst Time respectively A : 6 : 3; B : 7 : 2; C : 0 : 4; D : 2 : 7. \r\nA. C, D, B, A \r\nB. C, D, A, B \r\nC. C, B, D, A \r\nD. C, A, B, D \r\nANSWER: A \r\n \r\n49. Question 20: \r\nUsing FCFS (First Come First Served) algorithm, find the order of execution \r\nfor the following processes with the given data in the order Process : Arrival \r\nTime : Burst Time respectively A : 6 : 3; B : 7 : 2; C : 0 : 4; D : 2 : 7. \r\nA. C, B, A, D \r\nB. C, A, D, B \r\nC. C, D, B, A \r\nD. C, D, A, B \r\nANSWER: D \r\n \r\n \r\nCHAPTER 3 \r\n50. Question 1: \r\nWhat is the main purpose of memory management in an operating system? \r\nA. To organize CPU scheduling \r\nB. To allocate & deallocate memory space efficiently \r\nC. To manage input/output devices \r\nD. To protect data from viruses \r\nAnswer: B \r\n51. Question 2: \r\nWhich type of memory is directly accessible by the CPU? \r\nA. Secondary memory \r\nB. Cache memory \r\nC. Virtual memory \r\nD. Optical storage \r\nAnswer: B \r\n \r\n52. Question 3: \r\nThe technique of keeping only part of a program in memory is called__________. \r\nA. Swapping \r\nB. Paging \r\nC. Segmentation \r\nD. Virtual memory \r\nANSWER: D \r\n \r\n53. Question 3: \r\nIn paging, the memory is divided into fixed-size blocks called _______________. \r\nA. Frames \r\nB. Segments \r\nC. Pages \r\nD. Both A and C \r\nANSWER: D \r\n \r\n54. Question 4: \r\nIn a segmentation memory management system, how is memory divided? \r\nA. Into equal-sized memory blocks used for process allocation \r\nB. Into logical divisions such as functions, stacks, or data structures \r\nC. Into fixed-size pages defined by the hardware memory unit \r\nD. Into cache lines that temporarily store frequently used instructions \r\nAnswer: B \r\n \r\n55. Question 5: \r\nWhat is the process of moving a program temporarily from main memory to \r\nsecondary storage called? \r\nA. Spooling of input and output operations \r\nB. Swapping between main memory and secondary storage devices \r\nC. Paging operations that divide memory into frames and pages \r\nD. Segmentation of logical memory into distinct sections \r\nAnswer: B \r\n \r\n56. Question 6: \r\nWhat does the term internal fragmentation refer to in memory management? \r\nA. Wasted space inside allocated memory blocks that remains unused \r\nB. Unused free memory located between allocated memory regions \r\nC. A shortage of available memory for new process allocation \r\nD. Overlapping memory segments that cause data corruption \r\nAnswer: A \r\n \r\n57. Question 7: \r\nWhich of the following algorithms can be used for page replacement in operating \r\nsystems? \r\nA. First-In, First-Out (FIFO) algorithm used for page management \r\nB. Least Recently Used (LRU) algorithm that tracks page references \r\nC. Optimal algorithm that replaces the page not needed for the longest time \r\nD. All of the above algorithms are used for page replacement \r\nAnswer: D \r\n \r\n58. Question 8: \r\nWhat is the main purpose of the page table in a memory management system? \r\nA. To store process identifiers for CPU scheduling operations \r\nB. To map logical addresses generated by a program to physical addresses in \r\nmemory \r\nC. To maintain CPU scheduling and process prioritization data \r\nD. To control input and output operations of storage devices \r\nAnswer: B \r\n \r\n59. Question 9: \r\nWhich type of memory management allows a process to use more memory than \r\nthe amount of physical RAM available? \r\nA. Paging that divides memory into small fixed-size frames \r\nB. Virtual memory that extends physical memory through disk space usage \r\nC. Contiguous allocation that uses adjacent memory blocks for processes \r\nD. Segmentation that divides memory based on logical program structure \r\nAnswer: B \r\n \r\n60. Question 10: \r\nWhat is the main purpose of swapping in memory management systems? \r\nA. To permanently remove inactive processes from main memory space \r\nB. To temporarily move processes between main memory and secondary storage \r\nC. To increase CPU processing speed by reordering instructions \r\nD. To allocate cache memory dynamically during program execution \r\nAnswer: B \r\n \r\n61. Question 11: \r\nIn segmentation, what does each segment in memory represent? \r\nA. A fixed-size block of physical memory used for allocation \r\nB. A logical unit such as code, stack, or data used by a program \r\nC. A single machine instruction executed by the CPU \r\nD. A page frame used in paging memory systems \r\nAnswer: B \r\n \r\n62. Question 12: \r\nWhich of the following statements about static partitioning in memory \r\nmanagement is correct? \r\nA. Memory is divided into equal-sized partitions dynamically at runtime \r\nB. Partitions are created when the system starts and remain fixed in size \r\nC. Partition sizes change automatically depending on process requirements \r\nD. Static partitioning completely eliminates memory fragmentation issues \r\nAnswer: B \r\n \r\n63. Question 13: \r\nWhat is the main drawback of dynamic partitioning in memory allocation? \r\nA. It results in internal fragmentation within allocated partitions \r\nB. It leads to external fragmentation between allocated partitions \r\nC. It lacks flexibility for different process size requirements \r\nD. It reduces CPU performance due to complex memory management \r\nAnswer: B \r\n \r\n64. Question 14: \r\nIn a segmented memory system, what does the logical address consist of? \r\nA. A page number and an offset that determine the physical address \r\nB. A segment number and an offset within that specific memory segment \r\nC. A frame number and an offset calculated by the page table \r\nD. A block number and a displacement within the storage device \r\nAnswer: B \r\n65. Question 15:  \r\nA memory has partitions with sizes (in KB): 16, 53, 22, 37, 62, 44. If a process of size \r\n32 KB requests memory, which partition size (in KB) will be chosen if using the First \r\nFit method? \r\nA. 53 \r\nB. 62 \r\nC. 37 \r\nD. 44 \r\nANSWER: A \r\n \r\n	\N	\N	\N	\N	\N	\N	\N	\N	f
177	thinhnt.23it@vku.udn.vn	Hoang Dat	$2a$10$HOrSf/7tIi4KCDqGGqeA5.3/ddV99uE6bAcRMOcMsK9zaZHP6HvCy	CANDIDATE	\N	\N	\N	https://res.cloudinary.com/dgny2gq8p/raw/upload/v1777857815/ttjobs/cv/user-177-1777857812724	\N		2026-05-04 14:21:22.274815	Ho ten: Hoang Dat\nEmail: nguyenthanhthinh020812@gmail.com\nVi tri ung tuyen: IT\nMuc tieu nghe nghiep: JAVA\nKinh nghiem noi bat: java, Python\nKy nang: Docker, Python, Java, Github	IT	JAVA	java, Python	\N	\N	\N	\N	\N	f
371	candidate-94217253827800@test.local	test-candidate	dummy-hash	CANDIDATE	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	f
372	admin-94217453157400@test.local	test-admin	dummy-hash	ADMIN	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	f
373	recruiter-94217475768600@test.local	test-recruiter	dummy-hash	RECRUITER	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	f
374	recruiter-94217493087900@test.local	test-recruiter	dummy-hash	RECRUITER	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	f
375	candidate-94217540921700@test.local	test-candidate	dummy-hash	CANDIDATE	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	f
376	recruiter-94217568564400@test.local	test-recruiter	dummy-hash	RECRUITER	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	f
377	recruiter-94217661379000@test.local	test-recruiter	dummy-hash	RECRUITER	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	f
378	candidate-94217665422400@test.local	test-candidate	dummy-hash	CANDIDATE	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	f
379	candidate-94879629459800@test.local	test-candidate	dummy-hash	CANDIDATE	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	f
380	admin-94880331386700@test.local	test-admin	dummy-hash	ADMIN	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	f
381	recruiter-94880390378500@test.local	test-recruiter	dummy-hash	RECRUITER	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	f
382	recruiter-94880430425700@test.local	test-recruiter	dummy-hash	RECRUITER	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	f
383	candidate-94880717219000@test.local	test-candidate	dummy-hash	CANDIDATE	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	f
384	recruiter-94880816313400@test.local	test-recruiter	dummy-hash	RECRUITER	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	f
385	recruiter-94881220114500@test.local	test-recruiter	dummy-hash	RECRUITER	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	f
386	candidate-94881242744000@test.local	test-candidate	dummy-hash	CANDIDATE	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	f
\.


--
-- Name: application_ai_scores_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.application_ai_scores_id_seq', 10, true);


--
-- Name: candidate_job_matches_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.candidate_job_matches_id_seq', 60, true);


--
-- Name: career_guide_articles_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.career_guide_articles_id_seq', 4, true);


--
-- Name: companies_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.companies_id_seq', 163, true);


--
-- Name: company_follows_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.company_follows_id_seq', 2, true);


--
-- Name: company_members_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.company_members_id_seq', 45, true);


--
-- Name: company_reviews_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.company_reviews_id_seq', 1, false);


--
-- Name: conversations_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.conversations_id_seq', 3, true);


--
-- Name: cvs_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.cvs_id_seq', 1, false);


--
-- Name: email_change_verifications_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.email_change_verifications_id_seq', 6, true);


--
-- Name: interview_schedules_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.interview_schedules_id_seq', 2, true);


--
-- Name: job_alert_history_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.job_alert_history_id_seq', 1, false);


--
-- Name: job_application_status_audits_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.job_application_status_audits_id_seq', 18, true);


--
-- Name: job_applications_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.job_applications_id_seq', 10, true);


--
-- Name: jobs_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.jobs_id_seq', 306, true);


--
-- Name: message_attachments_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.message_attachments_id_seq', 7, true);


--
-- Name: messages_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.messages_id_seq', 21, true);


--
-- Name: notifications_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.notifications_id_seq', 39, true);


--
-- Name: recruiter_activity_logs_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.recruiter_activity_logs_id_seq', 33, true);


--
-- Name: recruitment_campaigns_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.recruitment_campaigns_id_seq', 1, true);


--
-- Name: recruitment_events_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.recruitment_events_id_seq', 1, false);


--
-- Name: saved_jobs_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.saved_jobs_id_seq', 3, true);


--
-- Name: skills_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.skills_id_seq', 36, true);


--
-- Name: user_cvs_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.user_cvs_id_seq', 6, true);


--
-- Name: user_tool_sessions_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.user_tool_sessions_id_seq', 1, false);


--
-- Name: users_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.users_id_seq', 386, true);


--
-- Name: application_ai_scores application_ai_scores_application_id_key; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.application_ai_scores
    ADD CONSTRAINT application_ai_scores_application_id_key UNIQUE (application_id);


--
-- Name: application_ai_scores application_ai_scores_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.application_ai_scores
    ADD CONSTRAINT application_ai_scores_pkey PRIMARY KEY (id);


--
-- Name: candidate_job_matches candidate_job_matches_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.candidate_job_matches
    ADD CONSTRAINT candidate_job_matches_pkey PRIMARY KEY (id);


--
-- Name: career_guide_articles career_guide_articles_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.career_guide_articles
    ADD CONSTRAINT career_guide_articles_pkey PRIMARY KEY (id);


--
-- Name: career_guide_articles career_guide_articles_slug_key; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.career_guide_articles
    ADD CONSTRAINT career_guide_articles_slug_key UNIQUE (slug);


--
-- Name: companies companies_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.companies
    ADD CONSTRAINT companies_pkey PRIMARY KEY (id);


--
-- Name: company_follows company_follows_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.company_follows
    ADD CONSTRAINT company_follows_pkey PRIMARY KEY (id);


--
-- Name: company_members company_members_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.company_members
    ADD CONSTRAINT company_members_pkey PRIMARY KEY (id);


--
-- Name: company_reviews company_reviews_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.company_reviews
    ADD CONSTRAINT company_reviews_pkey PRIMARY KEY (id);


--
-- Name: conversations conversations_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.conversations
    ADD CONSTRAINT conversations_pkey PRIMARY KEY (id);


--
-- Name: cvs cvs_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.cvs
    ADD CONSTRAINT cvs_pkey PRIMARY KEY (id);


--
-- Name: email_change_verifications email_change_verifications_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.email_change_verifications
    ADD CONSTRAINT email_change_verifications_pkey PRIMARY KEY (id);


--
-- Name: flyway_schema_history flyway_schema_history_pk; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.flyway_schema_history
    ADD CONSTRAINT flyway_schema_history_pk PRIMARY KEY (installed_rank);


--
-- Name: interview_schedules interview_schedules_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.interview_schedules
    ADD CONSTRAINT interview_schedules_pkey PRIMARY KEY (id);


--
-- Name: job_alert_history job_alert_history_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.job_alert_history
    ADD CONSTRAINT job_alert_history_pkey PRIMARY KEY (id);


--
-- Name: job_application_status_audits job_application_status_audits_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.job_application_status_audits
    ADD CONSTRAINT job_application_status_audits_pkey PRIMARY KEY (id);


--
-- Name: job_applications job_applications_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.job_applications
    ADD CONSTRAINT job_applications_pkey PRIMARY KEY (id);


--
-- Name: job_need_preferences job_need_preferences_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.job_need_preferences
    ADD CONSTRAINT job_need_preferences_pkey PRIMARY KEY (user_id);


--
-- Name: jobs jobs_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.jobs
    ADD CONSTRAINT jobs_pkey PRIMARY KEY (id);


--
-- Name: message_attachments message_attachments_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.message_attachments
    ADD CONSTRAINT message_attachments_pkey PRIMARY KEY (id);


--
-- Name: messages messages_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.messages
    ADD CONSTRAINT messages_pkey PRIMARY KEY (id);


--
-- Name: notification_preferences notification_preferences_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.notification_preferences
    ADD CONSTRAINT notification_preferences_pkey PRIMARY KEY (user_id);


--
-- Name: notifications notifications_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.notifications
    ADD CONSTRAINT notifications_pkey PRIMARY KEY (id);


--
-- Name: conversation_members pk_conversation_members; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.conversation_members
    ADD CONSTRAINT pk_conversation_members PRIMARY KEY (conversation_id, user_id);


--
-- Name: recruiter_activity_logs recruiter_activity_logs_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.recruiter_activity_logs
    ADD CONSTRAINT recruiter_activity_logs_pkey PRIMARY KEY (id);


--
-- Name: recruitment_campaign_applications recruitment_campaign_applications_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.recruitment_campaign_applications
    ADD CONSTRAINT recruitment_campaign_applications_pkey PRIMARY KEY (campaign_id, application_id);


--
-- Name: recruitment_campaign_jobs recruitment_campaign_jobs_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.recruitment_campaign_jobs
    ADD CONSTRAINT recruitment_campaign_jobs_pkey PRIMARY KEY (campaign_id, job_id);


--
-- Name: recruitment_campaigns recruitment_campaigns_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.recruitment_campaigns
    ADD CONSTRAINT recruitment_campaigns_pkey PRIMARY KEY (id);


--
-- Name: recruitment_events recruitment_events_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.recruitment_events
    ADD CONSTRAINT recruitment_events_pkey PRIMARY KEY (id);


--
-- Name: saved_jobs saved_jobs_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.saved_jobs
    ADD CONSTRAINT saved_jobs_pkey PRIMARY KEY (id);


--
-- Name: skills skills_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.skills
    ADD CONSTRAINT skills_pkey PRIMARY KEY (id);


--
-- Name: skills uk85woe63nu9klkk9fa73vf0jd0; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.skills
    ADD CONSTRAINT uk85woe63nu9klkk9fa73vf0jd0 UNIQUE (name);


--
-- Name: company_follows uq_company_follows_user_company; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.company_follows
    ADD CONSTRAINT uq_company_follows_user_company UNIQUE (user_id, company_id);


--
-- Name: company_members uq_company_members_company_user; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.company_members
    ADD CONSTRAINT uq_company_members_company_user UNIQUE (company_id, user_id);


--
-- Name: job_alert_history uq_job_alert_history_user_job; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.job_alert_history
    ADD CONSTRAINT uq_job_alert_history_user_job UNIQUE (user_id, job_id);


--
-- Name: saved_jobs uq_saved_jobs_user_job; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.saved_jobs
    ADD CONSTRAINT uq_saved_jobs_user_job UNIQUE (user_id, job_id);


--
-- Name: user_cvs user_cvs_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.user_cvs
    ADD CONSTRAINT user_cvs_pkey PRIMARY KEY (id);


--
-- Name: user_tool_sessions user_tool_sessions_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.user_tool_sessions
    ADD CONSTRAINT user_tool_sessions_pkey PRIMARY KEY (id);


--
-- Name: users users_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.users
    ADD CONSTRAINT users_pkey PRIMARY KEY (id);


--
-- Name: flyway_schema_history_s_idx; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX flyway_schema_history_s_idx ON public.flyway_schema_history USING btree (success);


--
-- Name: idx_application_ai_scores_score; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_application_ai_scores_score ON public.application_ai_scores USING btree (score DESC, scored_at DESC);


--
-- Name: idx_candidate_job_matches_user_version; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_candidate_job_matches_user_version ON public.candidate_job_matches USING btree (user_id, preference_updated_at, score DESC);


--
-- Name: idx_companies_deleted_at; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_companies_deleted_at ON public.companies USING btree (deleted_at);


--
-- Name: idx_company_follows_company_id; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_company_follows_company_id ON public.company_follows USING btree (company_id);


--
-- Name: idx_company_follows_user_followed_at; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_company_follows_user_followed_at ON public.company_follows USING btree (user_id, followed_at DESC);


--
-- Name: idx_company_members_company; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_company_members_company ON public.company_members USING btree (company_id);


--
-- Name: idx_company_members_user; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_company_members_user ON public.company_members USING btree (user_id);


--
-- Name: idx_company_reviews_company_created; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_company_reviews_company_created ON public.company_reviews USING btree (company_id, created_at DESC);


--
-- Name: idx_conversation_members_conversation; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_conversation_members_conversation ON public.conversation_members USING btree (conversation_id);


--
-- Name: idx_conversation_members_user; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_conversation_members_user ON public.conversation_members USING btree (user_id);


--
-- Name: idx_email_change_user_active; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_email_change_user_active ON public.email_change_verifications USING btree (user_id, new_email, expires_at DESC);


--
-- Name: idx_job_alert_history_user_sent; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_job_alert_history_user_sent ON public.job_alert_history USING btree (user_id, sent_at DESC);


--
-- Name: idx_job_app_status_audit_application; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_job_app_status_audit_application ON public.job_application_status_audits USING btree (application_id);


--
-- Name: idx_job_applications_job; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_job_applications_job ON public.job_applications USING btree (job_id);


--
-- Name: idx_job_applications_user; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_job_applications_user ON public.job_applications USING btree (user_id);


--
-- Name: idx_jobs_category; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_jobs_category ON public.jobs USING btree (category);


--
-- Name: idx_jobs_company_status; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_jobs_company_status ON public.jobs USING btree (company_id, status);


--
-- Name: idx_jobs_deleted_at; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_jobs_deleted_at ON public.jobs USING btree (deleted_at);


--
-- Name: idx_messages_conversation; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_messages_conversation ON public.messages USING btree (conversation_id);


--
-- Name: idx_messages_sender; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_messages_sender ON public.messages USING btree (sender_id);


--
-- Name: idx_notifications_user_created_at; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_notifications_user_created_at ON public.notifications USING btree (user_id, created_at DESC);


--
-- Name: idx_notifications_user_is_read; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_notifications_user_is_read ON public.notifications USING btree (user_id, is_read);


--
-- Name: idx_recruiter_activity_logs_actor_created_at; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_recruiter_activity_logs_actor_created_at ON public.recruiter_activity_logs USING btree (actor_id, created_at DESC);


--
-- Name: idx_recruiter_activity_logs_company; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_recruiter_activity_logs_company ON public.recruiter_activity_logs USING btree (company_id);


--
-- Name: idx_recruiter_activity_logs_job; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_recruiter_activity_logs_job ON public.recruiter_activity_logs USING btree (job_id);


--
-- Name: idx_saved_jobs_job_id; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_saved_jobs_job_id ON public.saved_jobs USING btree (job_id);


--
-- Name: idx_saved_jobs_user_saved_at; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_saved_jobs_user_saved_at ON public.saved_jobs USING btree (user_id, saved_at DESC);


--
-- Name: idx_tool_sessions_user; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_tool_sessions_user ON public.user_tool_sessions USING btree (user_id, created_at DESC);


--
-- Name: idx_tool_sessions_user_tool; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_tool_sessions_user_tool ON public.user_tool_sessions USING btree (user_id, tool_slug, created_at DESC);


--
-- Name: saved_jobs trg_saved_jobs_set_saved_at; Type: TRIGGER; Schema: public; Owner: postgres
--

CREATE TRIGGER trg_saved_jobs_set_saved_at BEFORE INSERT ON public.saved_jobs FOR EACH ROW EXECUTE FUNCTION public.set_saved_jobs_saved_at();


--
-- Name: candidate_job_matches candidate_job_matches_job_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.candidate_job_matches
    ADD CONSTRAINT candidate_job_matches_job_id_fkey FOREIGN KEY (job_id) REFERENCES public.jobs(id);


--
-- Name: company_follows company_follows_company_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.company_follows
    ADD CONSTRAINT company_follows_company_id_fkey FOREIGN KEY (company_id) REFERENCES public.companies(id) ON DELETE CASCADE;


--
-- Name: company_follows company_follows_user_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.company_follows
    ADD CONSTRAINT company_follows_user_id_fkey FOREIGN KEY (user_id) REFERENCES public.users(id) ON DELETE CASCADE;


--
-- Name: company_members company_members_company_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.company_members
    ADD CONSTRAINT company_members_company_id_fkey FOREIGN KEY (company_id) REFERENCES public.companies(id) ON DELETE CASCADE;


--
-- Name: company_members company_members_user_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.company_members
    ADD CONSTRAINT company_members_user_id_fkey FOREIGN KEY (user_id) REFERENCES public.users(id) ON DELETE CASCADE;


--
-- Name: company_reviews company_reviews_company_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.company_reviews
    ADD CONSTRAINT company_reviews_company_id_fkey FOREIGN KEY (company_id) REFERENCES public.companies(id) ON DELETE CASCADE;


--
-- Name: company_reviews company_reviews_user_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.company_reviews
    ADD CONSTRAINT company_reviews_user_id_fkey FOREIGN KEY (user_id) REFERENCES public.users(id) ON DELETE SET NULL;


--
-- Name: conversation_members conversation_members_conversation_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.conversation_members
    ADD CONSTRAINT conversation_members_conversation_id_fkey FOREIGN KEY (conversation_id) REFERENCES public.conversations(id) ON DELETE CASCADE;


--
-- Name: conversation_members conversation_members_user_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.conversation_members
    ADD CONSTRAINT conversation_members_user_id_fkey FOREIGN KEY (user_id) REFERENCES public.users(id) ON DELETE CASCADE;


--
-- Name: email_change_verifications email_change_verifications_user_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.email_change_verifications
    ADD CONSTRAINT email_change_verifications_user_id_fkey FOREIGN KEY (user_id) REFERENCES public.users(id) ON DELETE CASCADE;


--
-- Name: job_skills fk1gb74tysr9vkypwivvvovxq1f; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.job_skills
    ADD CONSTRAINT fk1gb74tysr9vkypwivvvovxq1f FOREIGN KEY (job_id) REFERENCES public.jobs(id);


--
-- Name: cvs fk8dmd6n9rd1bjnsae495lknrwj; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.cvs
    ADD CONSTRAINT fk8dmd6n9rd1bjnsae495lknrwj FOREIGN KEY (user_id) REFERENCES public.users(id);


--
-- Name: application_ai_scores fk_application_ai_scores_application; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.application_ai_scores
    ADD CONSTRAINT fk_application_ai_scores_application FOREIGN KEY (application_id) REFERENCES public.job_applications(id) ON DELETE CASCADE;


--
-- Name: recruitment_campaign_applications fk_campaign_applications_application; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.recruitment_campaign_applications
    ADD CONSTRAINT fk_campaign_applications_application FOREIGN KEY (application_id) REFERENCES public.job_applications(id) ON DELETE CASCADE;


--
-- Name: recruitment_campaign_applications fk_campaign_applications_campaign; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.recruitment_campaign_applications
    ADD CONSTRAINT fk_campaign_applications_campaign FOREIGN KEY (campaign_id) REFERENCES public.recruitment_campaigns(id) ON DELETE CASCADE;


--
-- Name: recruitment_campaign_jobs fk_campaign_jobs_campaign; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.recruitment_campaign_jobs
    ADD CONSTRAINT fk_campaign_jobs_campaign FOREIGN KEY (campaign_id) REFERENCES public.recruitment_campaigns(id) ON DELETE CASCADE;


--
-- Name: recruitment_campaign_jobs fk_campaign_jobs_job; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.recruitment_campaign_jobs
    ADD CONSTRAINT fk_campaign_jobs_job FOREIGN KEY (job_id) REFERENCES public.jobs(id) ON DELETE CASCADE;


--
-- Name: interview_schedules fk_interview_schedules_application; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.interview_schedules
    ADD CONSTRAINT fk_interview_schedules_application FOREIGN KEY (application_id) REFERENCES public.job_applications(id) ON DELETE CASCADE;


--
-- Name: interview_schedules fk_interview_schedules_candidate; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.interview_schedules
    ADD CONSTRAINT fk_interview_schedules_candidate FOREIGN KEY (candidate_id) REFERENCES public.users(id);


--
-- Name: interview_schedules fk_interview_schedules_recruiter; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.interview_schedules
    ADD CONSTRAINT fk_interview_schedules_recruiter FOREIGN KEY (recruiter_id) REFERENCES public.users(id);


--
-- Name: message_attachments fk_message_attachments_message; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.message_attachments
    ADD CONSTRAINT fk_message_attachments_message FOREIGN KEY (message_id) REFERENCES public.messages(id) ON DELETE CASCADE;


--
-- Name: recruitment_campaigns fk_recruitment_campaigns_company; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.recruitment_campaigns
    ADD CONSTRAINT fk_recruitment_campaigns_company FOREIGN KEY (company_id) REFERENCES public.companies(id) ON DELETE CASCADE;


--
-- Name: recruitment_campaigns fk_recruitment_campaigns_created_by; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.recruitment_campaigns
    ADD CONSTRAINT fk_recruitment_campaigns_created_by FOREIGN KEY (created_by_id) REFERENCES public.users(id);


--
-- Name: recruitment_events fk_recruitment_events_actor; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.recruitment_events
    ADD CONSTRAINT fk_recruitment_events_actor FOREIGN KEY (actor_id) REFERENCES public.users(id) ON DELETE SET NULL;


--
-- Name: recruitment_events fk_recruitment_events_application; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.recruitment_events
    ADD CONSTRAINT fk_recruitment_events_application FOREIGN KEY (application_id) REFERENCES public.job_applications(id) ON DELETE SET NULL;


--
-- Name: recruitment_events fk_recruitment_events_company; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.recruitment_events
    ADD CONSTRAINT fk_recruitment_events_company FOREIGN KEY (company_id) REFERENCES public.companies(id) ON DELETE SET NULL;


--
-- Name: recruitment_events fk_recruitment_events_job; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.recruitment_events
    ADD CONSTRAINT fk_recruitment_events_job FOREIGN KEY (job_id) REFERENCES public.jobs(id) ON DELETE SET NULL;


--
-- Name: job_skills fke10ho7um0atjm67b9dgokmfyx; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.job_skills
    ADD CONSTRAINT fke10ho7um0atjm67b9dgokmfyx FOREIGN KEY (skill_id) REFERENCES public.skills(id);


--
-- Name: user_skills fkh223y61gwijpgqt6nlsuti07g; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.user_skills
    ADD CONSTRAINT fkh223y61gwijpgqt6nlsuti07g FOREIGN KEY (skill_id) REFERENCES public.skills(id);


--
-- Name: companies fkmetlbmw6om0v8dkknxihl19a3; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.companies
    ADD CONSTRAINT fkmetlbmw6om0v8dkknxihl19a3 FOREIGN KEY (created_by) REFERENCES public.users(id);


--
-- Name: job_applications fkqs2guhg7p83917vto86imuthy; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.job_applications
    ADD CONSTRAINT fkqs2guhg7p83917vto86imuthy FOREIGN KEY (user_id) REFERENCES public.users(id);


--
-- Name: job_applications fkqt4m3c9yiioi16kwsyjrl0cpl; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.job_applications
    ADD CONSTRAINT fkqt4m3c9yiioi16kwsyjrl0cpl FOREIGN KEY (job_id) REFERENCES public.jobs(id);


--
-- Name: user_skills fkro13if9r7fwkr5115715127ai; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.user_skills
    ADD CONSTRAINT fkro13if9r7fwkr5115715127ai FOREIGN KEY (user_id) REFERENCES public.users(id);


--
-- Name: jobs fkrtmqcrktb6s7xq8djbs2a2war; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.jobs
    ADD CONSTRAINT fkrtmqcrktb6s7xq8djbs2a2war FOREIGN KEY (company_id) REFERENCES public.companies(id);


--
-- Name: job_alert_history job_alert_history_job_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.job_alert_history
    ADD CONSTRAINT job_alert_history_job_id_fkey FOREIGN KEY (job_id) REFERENCES public.jobs(id) ON DELETE CASCADE;


--
-- Name: job_alert_history job_alert_history_user_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.job_alert_history
    ADD CONSTRAINT job_alert_history_user_id_fkey FOREIGN KEY (user_id) REFERENCES public.users(id) ON DELETE CASCADE;


--
-- Name: job_application_status_audits job_application_status_audits_application_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.job_application_status_audits
    ADD CONSTRAINT job_application_status_audits_application_id_fkey FOREIGN KEY (application_id) REFERENCES public.job_applications(id) ON DELETE CASCADE;


--
-- Name: job_application_status_audits job_application_status_audits_changed_by_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.job_application_status_audits
    ADD CONSTRAINT job_application_status_audits_changed_by_id_fkey FOREIGN KEY (changed_by_id) REFERENCES public.users(id);


--
-- Name: job_applications job_applications_cv_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.job_applications
    ADD CONSTRAINT job_applications_cv_id_fkey FOREIGN KEY (cv_id) REFERENCES public.user_cvs(id);


--
-- Name: messages messages_conversation_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.messages
    ADD CONSTRAINT messages_conversation_id_fkey FOREIGN KEY (conversation_id) REFERENCES public.conversations(id) ON DELETE CASCADE;


--
-- Name: messages messages_sender_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.messages
    ADD CONSTRAINT messages_sender_id_fkey FOREIGN KEY (sender_id) REFERENCES public.users(id) ON DELETE CASCADE;


--
-- Name: notification_preferences notification_preferences_user_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.notification_preferences
    ADD CONSTRAINT notification_preferences_user_id_fkey FOREIGN KEY (user_id) REFERENCES public.users(id) ON DELETE CASCADE;


--
-- Name: notifications notifications_user_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.notifications
    ADD CONSTRAINT notifications_user_id_fkey FOREIGN KEY (user_id) REFERENCES public.users(id) ON DELETE CASCADE;


--
-- Name: recruiter_activity_logs recruiter_activity_logs_actor_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.recruiter_activity_logs
    ADD CONSTRAINT recruiter_activity_logs_actor_id_fkey FOREIGN KEY (actor_id) REFERENCES public.users(id) ON DELETE CASCADE;


--
-- Name: recruiter_activity_logs recruiter_activity_logs_application_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.recruiter_activity_logs
    ADD CONSTRAINT recruiter_activity_logs_application_id_fkey FOREIGN KEY (application_id) REFERENCES public.job_applications(id) ON DELETE SET NULL;


--
-- Name: recruiter_activity_logs recruiter_activity_logs_company_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.recruiter_activity_logs
    ADD CONSTRAINT recruiter_activity_logs_company_id_fkey FOREIGN KEY (company_id) REFERENCES public.companies(id) ON DELETE SET NULL;


--
-- Name: recruiter_activity_logs recruiter_activity_logs_job_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.recruiter_activity_logs
    ADD CONSTRAINT recruiter_activity_logs_job_id_fkey FOREIGN KEY (job_id) REFERENCES public.jobs(id) ON DELETE SET NULL;


--
-- Name: saved_jobs saved_jobs_job_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.saved_jobs
    ADD CONSTRAINT saved_jobs_job_id_fkey FOREIGN KEY (job_id) REFERENCES public.jobs(id) ON DELETE CASCADE;


--
-- Name: saved_jobs saved_jobs_user_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.saved_jobs
    ADD CONSTRAINT saved_jobs_user_id_fkey FOREIGN KEY (user_id) REFERENCES public.users(id) ON DELETE CASCADE;


--
-- Name: user_cvs user_cvs_user_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.user_cvs
    ADD CONSTRAINT user_cvs_user_id_fkey FOREIGN KEY (user_id) REFERENCES public.users(id);


--
-- Name: user_tool_sessions user_tool_sessions_user_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.user_tool_sessions
    ADD CONSTRAINT user_tool_sessions_user_id_fkey FOREIGN KEY (user_id) REFERENCES public.users(id) ON DELETE CASCADE;


--
-- Name: SCHEMA public; Type: ACL; Schema: -; Owner: postgres
--

REVOKE USAGE ON SCHEMA public FROM PUBLIC;


--
-- PostgreSQL database dump complete
--

\unrestrict fI2CFUsOo3OrgLV6Kiet7K29HNDaydhcFIZfkuwgvsSO38n1Z5nBzrPNxDfk7iK

