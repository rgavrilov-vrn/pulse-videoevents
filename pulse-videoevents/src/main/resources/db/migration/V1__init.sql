CREATE EXTENSION IF NOT EXISTS pgcrypto;
CREATE TABLE IF NOT EXISTS incidents (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  incident_number VARCHAR(30) NOT NULL UNIQUE,
  external_event_id VARCHAR(500) NOT NULL UNIQUE,
  status VARCHAR(30) NOT NULL,
  severity VARCHAR(20) NOT NULL,
  event_time TIMESTAMPTZ NOT NULL,
  object_id UUID, camera_id UUID, operator_id UUID, assignee_id UUID,
  due_at TIMESTAMPTZ, resolution_comment TEXT,
  version BIGINT NOT NULL DEFAULT 1,
  created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
  updated_at TIMESTAMPTZ NOT NULL DEFAULT now()
);
CREATE TABLE IF NOT EXISTS incident_evidence (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  incident_id UUID NOT NULL REFERENCES incidents(id) ON DELETE CASCADE,
  media_asset_id UUID NOT NULL,
  evidence_role VARCHAR(40) NOT NULL,
  file_name VARCHAR(500) NOT NULL,
  sha256 CHAR(64) NOT NULL,
  captured_at TIMESTAMPTZ,
  created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
  CONSTRAINT uq_incident_evidence_media UNIQUE (incident_id, media_asset_id)
);
CREATE TABLE IF NOT EXISTS video_server_events (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  external_event_id VARCHAR(500) NOT NULL UNIQUE,
  event_timestamp TIMESTAMPTZ NOT NULL,
  event_type VARCHAR(100) NOT NULL,
  object_external_id VARCHAR(255) NOT NULL,
  camera_external_id VARCHAR(255),
  server_address VARCHAR(255),
  raw_payload JSONB NOT NULL DEFAULT '{}'::jsonb,
  incident_id UUID REFERENCES incidents(id),
  received_at TIMESTAMPTZ NOT NULL DEFAULT now(),
  processed_at TIMESTAMPTZ
);
