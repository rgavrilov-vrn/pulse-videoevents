package ru.agroeco.pulse.videoevents.domain.event;

import java.time.Instant;
import java.util.UUID;

public record IncidentConfirmed(UUID incidentId, Instant occurredAt) implements

DomainEvent {
    @Override public String eventType () {
        return "IncidentConfirmed";
    }
}
