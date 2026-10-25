package ru.agroeco.pulse.videoevents.adapter.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface IncidentRepository extends JpaRepository<IncidentEntity, UUID> {
    Optional<IncidentEntity> findByExternalEventId(String externalEventId);

    @Query(value = "select nextval('incident_number_seq')", nativeQuery = true)
    long nextIncidentNumber();

    // PostgreSQL transaction-scoped lock; callers must use a write transaction.
    // Hash collisions merely serialize unrelated keys and do not weaken uniqueness.
    @Query(value = "select 1 from pg_advisory_xact_lock(hashtextextended(:eventKey, 0))", nativeQuery = true)
    int lockExternalEvent(@Param("eventKey") String eventKey);
}
