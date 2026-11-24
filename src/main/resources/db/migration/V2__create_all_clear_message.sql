-- Admin Automation (Notifications tab): editable "all clear" messages sent with the
-- long-absence report when no student has a long absence.
-- Matches entity com.smsweb.sms.models.automation.AllClearMessage.
-- Idempotent: safe if the table was already created by hand. No existing table or row is touched.
-- The six built-in messages are copied in by the application the first time a school opens the
-- Notifications tab, so no seed rows are needed here.
CREATE TABLE IF NOT EXISTS all_clear_message (
    id            BIGINT        NOT NULL AUTO_INCREMENT,
    school_id     BIGINT        NOT NULL,
    message_text  VARCHAR(200)  NOT NULL,
    is_active     BIT(1)        NOT NULL,
    created_on    DATETIME(6)   NULL,
    PRIMARY KEY (id),
    KEY idx_all_clear_school (school_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
