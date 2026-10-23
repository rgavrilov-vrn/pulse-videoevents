CREATE INDEX IF NOT EXISTS ix_incidents_status_event_time ON incidents(status, event_time DESC);
CREATE INDEX IF NOT EXISTS ix_incidents_assignee_status ON incidents(assignee_id, status);
CREATE INDEX IF NOT EXISTS ix_video_server_events_external_event ON video_server_events(external_event_id);
