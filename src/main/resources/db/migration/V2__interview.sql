CREATE TABLE interview_session (
    id                     BIGSERIAL PRIMARY KEY,
    session_id             VARCHAR(36)  NOT NULL UNIQUE,
    skill_id               VARCHAR(64)  NOT NULL,
    difficulty             VARCHAR(16),
    total_questions        INTEGER      NOT NULL,
    current_question_index INTEGER      NOT NULL DEFAULT 0,
    questions_json         TEXT         NOT NULL,
    status                 VARCHAR(20)  NOT NULL,
    created_at             TIMESTAMPTZ  NOT NULL DEFAULT now(),
    completed_at           TIMESTAMPTZ,
    CONSTRAINT interview_session_status_check
        CHECK (status IN ('IN_PROGRESS', 'COMPLETED'))
);

CREATE TABLE interview_answer (
    id             BIGSERIAL PRIMARY KEY,
    session_id     BIGINT       NOT NULL REFERENCES interview_session (id),
    question_index INTEGER      NOT NULL,
    question       TEXT,
    category       VARCHAR(64),
    user_answer    TEXT,
    answered_at    TIMESTAMPTZ  NOT NULL DEFAULT now(),
    CONSTRAINT uk_interview_answer_session_question UNIQUE (session_id, question_index)
);
