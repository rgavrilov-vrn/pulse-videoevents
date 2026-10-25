CREATE TABLE IF NOT EXISTS audit_events
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
    incident_id UUID,
    payload JSONB NOT NULL DEFAULT '{}'::jsonb,
    occurred_at TIMESTAMPTZ NOT NULL DEFAULT now
(
)
    );
CREATE INDEX IF NOT EXISTS ix_audit_events_incident ON audit_events(incident_id, occurred_at DESC);
