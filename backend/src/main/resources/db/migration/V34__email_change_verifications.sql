CREATE TABLE IF NOT EXISTS email_change_verifications (
    id          BIGSERIAL PRIMARY KEY,
    user_id     BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    new_email   VARCHAR(255) NOT NULL,
    code        VARCHAR(12) NOT NULL,
    expires_at  TIMESTAMP NOT NULL,
    used_at     TIMESTAMP,
    created_at  TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_email_change_user_active
    ON email_change_verifications(user_id, new_email, expires_at DESC);
