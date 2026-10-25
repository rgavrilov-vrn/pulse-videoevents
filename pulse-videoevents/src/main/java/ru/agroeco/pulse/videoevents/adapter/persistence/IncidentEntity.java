package ru.agroeco.pulse.videoevents.adapter.persistence;

import jakarta.persistence.*;
import lombok.*;
import ru.agroeco.pulse.videoevents.domain.IncidentStatus;
import ru.agroeco.pulse.videoevents.domain.Severity;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "incidents")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class IncidentEntity {
    @Id
    private UUID id;
    @Column(name = "incident_number", nullable = false, unique = true)
    private String incidentNumber;
    @Column(name = "external_event_id", nullable = false, unique = true)
    private String externalEventId;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private IncidentStatus status;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Severity severity;
    @Column(name = "event_time", nullable = false)
    private Instant eventTime;
    @Column(name = "object_id")
    private UUID objectId;
    @Column(name = "camera_id")
    private UUID cameraId;
    @Column(name = "operator_id")
    private UUID operatorId;
    @Column(name = "assignee_id")
    private UUID assigneeId;
    @Column(name = "due_at")
    private Instant dueAt;
    @Column(name = "resolution_comment")
    private String resolutionComment;
    @Version
    @Column(nullable = false)
    private long version;
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @PrePersist
    void prePersist() {
        if (id == null) id = UUID.randomUUID();
        if (createdAt == null) createdAt = Instant.now();
        updatedAt = Instant.now();
    }

    @PreUpdate
    void preUpdate() {
        updatedAt = Instant.now();
    }
}
