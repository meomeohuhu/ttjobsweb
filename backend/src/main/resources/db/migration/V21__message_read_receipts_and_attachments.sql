ALTER TABLE conversation_members
    ADD COLUMN last_read_at TIMESTAMP NULL;

UPDATE conversation_members
SET last_read_at = created_at
WHERE last_read_at IS NULL;

CREATE TABLE message_attachments (
    id BIGSERIAL PRIMARY KEY,
    message_id BIGINT NOT NULL,
    file_name VARCHAR(255),
    file_url TEXT NOT NULL,
    public_id VARCHAR(255),
    mime_type VARCHAR(255),
    file_size BIGINT,
    created_at TIMESTAMP NOT NULL,
    CONSTRAINT fk_message_attachments_message
        FOREIGN KEY (message_id) REFERENCES messages(id)
        ON DELETE CASCADE
);
