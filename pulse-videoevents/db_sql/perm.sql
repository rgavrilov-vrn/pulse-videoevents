-- 005_permissions.sql
-- Выполняется владельцем базы или администратором.

GRANT USAGE ON SCHEMA auth, ref, incident, audit, outbox TO pulse_videoevents_app;
GRANT SELECT, INSERT, UPDATE, DELETE ON ALL TABLES IN SCHEMA auth, ref, incident, outbox TO pulse_videoevents_app;

GRANT SELECT ON ALL TABLES IN SCHEMA audit, outbox TO pulse_videoevents_app;

GRANT USAGE, SELECT ON ALL SEQUENCES IN SCHEMA incident TO pulse_videoevents_app;
GRANT EXECUTE ON ALL FUNCTIONS IN SCHEMA incident TO pulse_videoevents_app;

GRANT USAGE ON SCHEMA ref, incident TO pulse_videoevents_readonly;
GRANT SELECT ON ALL TABLES IN SCHEMA ref, incident TO pulse_videoevents_readonly;
GRANT SELECT ON ALL TABLES IN SCHEMA audit, outbox TO pulse_videoevents_readonly;
GRANT SELECT ON incident.incident_list TO pulse_videoevents_readonly;

ALTER DEFAULT PRIVILEGES IN SCHEMA auth, ref, incident,outbox
GRANT SELECT, INSERT, UPDATE, DELETE ON TABLES TO pulse_videoevents_app;

ALTER DEFAULT PRIVILEGES IN SCHEMA incident
GRANT USAGE, SELECT ON SEQUENCES TO pulse_videoevents_app;

ALTER DEFAULT PRIVILEGES IN SCHEMA ref, incident
GRANT SELECT ON TABLES TO pulse_videoevents_readonly;
