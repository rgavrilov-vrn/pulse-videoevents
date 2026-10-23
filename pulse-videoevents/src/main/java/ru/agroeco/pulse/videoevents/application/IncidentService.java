package ru.agroeco.pulse.videoevents.application;

import ru.agroeco.pulse.videoevents.adapter.persistence.*;
import ru.agroeco.pulse.videoevents.application.dto.*;
import ru.agroeco.pulse.videoevents.application.event.EventDispatcher;
import ru.agroeco.pulse.videoevents.domain.IncidentStatus;
import ru.agroeco.pulse.videoevents.domain.Severity;
import ru.agroeco.pulse.videoevents.domain.event.*;
import ru.agroeco.pulse.videoevents.domain.exception.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class IncidentService {
    private final IncidentRepository incidents;
    private final EvidenceRepository evidence;
    private final VideoServerEventRepository videoEvents;
    private final EventDispatcher events;

    @Transactional
    public IncidentResponse ingestVideoEvent(VideoEventRequest r) {
        Objects.requireNonNull(r, "Video event is required");
        if (r.externalEventId() == null || r.externalEventId().isBlank()) {
            throw new IllegalArgumentException("External event ID is required");
        }
        // Current DB contract uses globally unique externalEventId. Source-scoped IDs
        // require a separate API and schema migration rather than silently changing it.
        incidents.lockExternalEvent(r.externalEventId());
        var existing = incidents.findByExternalEventId(r.externalEventId());
        if (existing.isPresent()) {
            log.info("Returning existing incident for event {}", r.externalEventId());
            return toResponse(existing.get());
        }
        Instant eventTime = r.eventTimestamp() == null ? Instant.now() : r.eventTimestamp();
        var source = VideoServerEventEntity.builder()
            .externalEventId(r.externalEventId()).eventTimestamp(eventTime).eventType(r.eventType())
            .objectExternalId(r.objectExternalId()).cameraExternalId(r.cameraExternalId())
            .serverAddress(r.serverAddress())
            .rawPayload(r.payload() == null ? "{}" : JsonCodec.write(r.payload())).build();
        videoEvents.save(source);
        long number = incidents.nextIncidentNumber();
        var incident = IncidentEntity.builder()
            .incidentNumber("INC-%d-%06d".formatted(LocalDate.now(ZoneOffset.UTC).getYear(), number))
            .externalEventId(r.externalEventId()).status(IncidentStatus.RECORDED).severity(Severity.MEDIUM)
            .eventTime(eventTime)
            .objectId(UUID.nameUUIDFromBytes(r.objectExternalId().getBytes(StandardCharsets.UTF_8)))
            .cameraId(r.cameraExternalId() == null ? null
                : UUID.nameUUIDFromBytes(r.cameraExternalId().getBytes(StandardCharsets.UTF_8))).build();
        incident = incidents.save(incident);
        source.setIncidentId(incident.getId());
        source.setProcessedAt(Instant.now());
        videoEvents.save(source);
        events.dispatch(new IncidentCreated(incident.getId(), r.externalEventId(), Instant.now()));
        return toResponse(incident);
    }

    @Transactional(readOnly = true)
    public List<IncidentResponse> findAll() {
        return incidents.findAll().stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public IncidentResponse findById(UUID id) { return toResponse(load(id)); }

    @Transactional
    public IncidentResponse attachEvidence(UUID id, EvidenceRequest r) {
        var incident = load(id);
        ensureOpen(incident);
        if (evidence.existsByIncidentIdAndMediaAssetId(id, r.mediaAssetId())) {
            throw new BusinessRuleException("Evidence already attached");
        }
        evidence.save(EvidenceEntity.builder().incidentId(id).mediaAssetId(r.mediaAssetId())
            .role(r.role()).fileName(r.fileName()).sha256(r.sha256()).capturedAt(r.capturedAt()).build());
        events.dispatch(new IncidentEvidenceAttached(id, r.mediaAssetId(), Instant.now()));
        return toResponse(incident);
    }

    @Transactional
    public IncidentResponse assign(UUID id, AssignRequest r) {
        var incident = load(id);
        ensureOpen(incident);
        if (r.dueAt() != null && r.dueAt().isBefore(Instant.now())) {
            throw new BusinessRuleException("Due date must not be in the past");
        }
        incident.setAssigneeId(r.assigneeId());
        incident.setDueAt(r.dueAt());
        var saved = incidents.save(incident);
        events.dispatch(new IncidentAssigned(id, r.assigneeId(), Instant.now()));
        return toResponse(saved);
    }

    @Transactional
    public IncidentResponse confirm(UUID id) {
        var incident = load(id);
        transition(incident, IncidentStatus.CONFIRMED);
        var saved = incidents.save(incident);
        events.dispatch(new IncidentConfirmed(id, Instant.now()));
        return toResponse(saved);
    }

    @Transactional
    public IncidentResponse reject(UUID id) {
        var incident = load(id);
        transition(incident, IncidentStatus.REJECTED);
        var saved = incidents.save(incident);
        events.dispatch(new IncidentRejected(id, Instant.now()));
        return toResponse(saved);
    }

    @Transactional
    public IncidentResponse close(UUID id, CloseRequest r) {
        if (r.resolutionComment() == null || r.resolutionComment().isBlank()) {
            throw new BusinessRuleException("Resolution is required");
        }
        var incident = load(id);
        transition(incident, IncidentStatus.CLOSED);
        incident.setResolutionComment(r.resolutionComment());
        var saved = incidents.save(incident);
        events.dispatch(new IncidentClosed(id, r.resolutionComment(), Instant.now()));
        return toResponse(saved);
    }

    private void transition(IncidentEntity incident, IncidentStatus target) {
        ensureOpen(incident);
        if (!incident.getStatus().canTransitionTo(target)) {
            throw new BusinessRuleException("Cannot transition from %s to %s".formatted(incident.getStatus(), target));
        }
        incident.setStatus(target);
    }

    private void ensureOpen(IncidentEntity incident) {
        if (incident.getStatus() == IncidentStatus.CLOSED || incident.getStatus() == IncidentStatus.CANCELLED) {
            throw new BusinessRuleException("Incident is closed or cancelled");
        }
    }

    private IncidentEntity load(UUID id) {
        return incidents.findById(id).orElseThrow(() -> new NotFoundException("Incident not found: " + id));
    }

    private IncidentResponse toResponse(IncidentEntity i) {
        return new IncidentResponse(i.getId(), i.getIncidentNumber(), i.getExternalEventId(), i.getStatus(),
            i.getSeverity(), i.getEventTime(), i.getObjectId(), i.getCameraId(), i.getOperatorId(),
            i.getAssigneeId(), i.getDueAt(), i.getResolutionComment(), i.getVersion());
    }
}
