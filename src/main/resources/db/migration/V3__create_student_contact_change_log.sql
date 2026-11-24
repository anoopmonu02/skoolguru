-- Audit trail for Mobile 1 changes made from Fee Submission > Update Contact.
-- Matches entity com.smsweb.sms.models.student.StudentContactChangeLog.
-- Idempotent and additive: no existing table or row is touched. Plain id columns, no foreign keys.
CREATE TABLE IF NOT EXISTS student_contact_change_log (
    id                     BIGINT       NOT NULL AUTO_INCREMENT,
    student_id             BIGINT       NOT NULL,
    school_id              BIGINT       NULL,
    academic_year_id       BIGINT       NULL,
    old_mobile             VARCHAR(20)  NULL,
    new_mobile             VARCHAR(20)  NULL,
    old_family_account_id  BIGINT       NULL,
    new_family_account_id  BIGINT       NULL,
    sessions_revoked       BIT(1)       NOT NULL,
    source                 VARCHAR(30)  NULL,
    changed_by             VARCHAR(100) NULL,
    changed_at             DATETIME(6)  NULL,
    PRIMARY KEY (id),
    KEY idx_sccl_student (student_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
