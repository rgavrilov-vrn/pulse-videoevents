# Outbox delivery contract

This commit is one part of the consolidated reliability PR, not a complete remediation.

- Pending PostgreSQL rows are selected with FOR UPDATE SKIP LOCKED and the configured batch size. Locks are held through bounded Kafka publication and the database commit. This is intentionally a simple relay; it occupies a DB connection during sends.
- SENT is recorded only after the Kafka send future confirms success. Timeout or failure schedules another attempt; after max-attempts the row stays FAILED for operator intervention.
- A crash after Kafka acknowledgement but before DB commit can publish a duplicate. Delivery is at-least-once, NOT exactly-once.
- Envelope eventId is the persistent outbox UUID and remains identical across retries. schemaVersion is 1. recordedAt is the outbox insertion time, not necessarily the domain occurrence time.
- The sample consumer uses a PostgreSQL inbox keyed by consumer name and eventId. Any future DB side effects must share that inbox transaction. Logging itself is not exactly-once.
- Unhandled consumer records retry indefinitely and block their partition instead of being discarded. Production operation should add a monitored quarantine/replay process.
- Retry FAILED records only after investigating: UPDATE outbox_events SET status='NEW', attempts=0, next_attempt_at=now(), last_error=NULL WHERE id='<event UUID>' AND status='FAILED';
- Existing Kafka records without an envelope require an explicit compatibility/migration plan; this consumer deliberately rejects them. Use a fresh development topic or migrate old records before enabling it.

Still to fix in the same PR: JSONB persistence and serialization, database sequence, concurrent ingestion, PostgreSQL integration tests and CI, security/configuration, module boundaries and pagination. No Maven/Docker test execution is claimed by this commit.
