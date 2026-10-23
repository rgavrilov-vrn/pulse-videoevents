package ru.agroeco.pulse.videoevents.adapter.persistence;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import java.time.Instant;
import java.util.UUID;

@Entity @Table(name = "audit_events")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class AuditEventEntity {
    @Id private UUID id;
    @Column(name = "event_type", nullable = false) private String eventType;
    @Column(name = "incident_id") private UUID incidentId;
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(nullable = false, columnDefinition = "jsonb") private String payload;
    @Column(name = "occurred_at", nullable = false) private Instant occurredAt;
    @PrePersist void prePersist() { if (id == null) id = UUID.randomUUID(); }
}
