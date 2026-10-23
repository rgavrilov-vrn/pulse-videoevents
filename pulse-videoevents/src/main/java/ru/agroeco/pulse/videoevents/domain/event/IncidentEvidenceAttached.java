package ru.agroeco.pulse.videoevents.domain.event;

import java.time.Instant;
import java.util.UUID;

public record IncidentEvidenceAttached(UUID incidentId, UUID mediaAssetId, Instant occurredAt) implements DomainEvent {
    @Override public String eventType() { return "IncidentEvidenceAttached"; }
}
