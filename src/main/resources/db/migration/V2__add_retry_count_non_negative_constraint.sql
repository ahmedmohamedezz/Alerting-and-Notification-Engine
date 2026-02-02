ALTER TABLE logs
ADD CONSTRAINT chk_logs_retry_count_non_negative
CHECK (retry_count >= 0);
