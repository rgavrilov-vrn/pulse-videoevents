package ru.agroeco.pulse.videoevents.domain.event;

import java.time.Instant;
import java.util.UUID;

public record IncidentClosed(UUID incidentId, String resolution, Instant occurredAt) implements DomainEvent {
    @Override public String eventType() { return "IncidentClosed"; }
}
