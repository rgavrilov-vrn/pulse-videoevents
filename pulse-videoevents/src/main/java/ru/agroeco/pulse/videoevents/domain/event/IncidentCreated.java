package ru.agroeco.pulse.videoevents.domain.event;

import java.time.Instant;
import java.util.UUID;

public record IncidentCreated(UUID incidentId, String externalEventId, Instant occurredAt) implements DomainEvent {
    @Override public String eventType() { return "IncidentCreated"; }
}
