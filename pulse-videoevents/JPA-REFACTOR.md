# JPA persistence refactor

Outbox listener/relay, audit listener and Kafka consumer inbox no longer depend on JdbcTemplate. JSON payloads in
OutboxEventEntity and AuditEventEntity use Hibernate 6 JSON mapping and retain their existing serialized String API.
Inbox deduplication uses a PostgreSQL native INSERT ON CONFLICT executed via a JPA repository. No schema migration is
needed for these mappings.

This is NOT the complete stabilization previously discussed. The relay deliberately retains a DB transaction and row
locks while waiting for bounded Kafka sends. Lease-based claiming, short transactions, aggregate ordering, production
security, PostgreSQL test migration and CI are still pending. Delivery remains at-least-once. Do not merge as a claim of
production readiness.

Existing source-event ColumnTransformer and IncidentRepository native sequence/advisory-lock queries remain JPA-based
and are not JDBC calls. Native SQL is intentionally retained where PostgreSQL semantics are required.

Unit tests cover ACK, failed future, timeout and exhausted attempts. They do not prove PostgreSQL locking, JSONB
roundtrip or rollback behavior. Maven and Docker have not been run for this change. The existing H2 integration profile
and other known issues are not fixed here.
