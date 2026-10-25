CREATE TABLE IF NOT EXISTS outbox_events
(
    id
    UUID
    PRIMARY
    KEY
    DEFAULT
    gen_random_uuid
(
),
    event_type VARCHAR
(
    100
) NOT NULL,
    incident_id UUID NOT NULL,
    payload JSONB NOT NULL,
    status VARCHAR
(
    20
) NOT NULL DEFAULT 'NEW',
    attempts INT NOT NULL DEFAULT 0,
    next_attempt_at TIMESTAMPTZ NOT NULL DEFAULT now
(
),
    published_at TIMESTAMPTZ,
    last_error TEXT,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now
(
)
    );
CREATE INDEX IF NOT EXISTS ix_outbox_events_status_next_attempt
    ON outbox_events(status, next_attempt_at);
