CREATE TABLE correction_tasks
(
    id             UUID PRIMARY KEY,
    text           TEXT        NOT NULL,
    language       VARCHAR(2)  NOT NULL,
    status         VARCHAR(20) NOT NULL,
    corrected_text TEXT,
    error_message  VARCHAR(1000),
    created_at     TIMESTAMP   NOT NULL,
    updated_at     TIMESTAMP   NOT NULL
);

CREATE INDEX idx_correction_tasks_status_created ON correction_tasks (status, created_at);
