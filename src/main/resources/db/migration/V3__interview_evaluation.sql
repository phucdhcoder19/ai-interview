ALTER TABLE interview_answer
    ADD COLUMN score            INTEGER,
    ADD COLUMN feedback         TEXT,
    ADD COLUMN key_points_json  TEXT,
    ADD COLUMN reference_answer TEXT;

ALTER TABLE interview_session
    ADD COLUMN evaluate_status   VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    ADD COLUMN overall_score     INTEGER,
    ADD COLUMN overall_feedback  TEXT,
    ADD COLUMN strengths_json    TEXT,
    ADD COLUMN improvements_json TEXT;

ALTER TABLE interview_session
    ADD CONSTRAINT interview_session_evaluate_status_check
        CHECK (evaluate_status IN ('PENDING', 'PROCESSING', 'COMPLETED', 'FAILED'));
