package ru.agroeco.pulse.videoevents.domain.event;

import java.time.Instant;
import java.util.UUID;

import ru.agroeco.pulse.videoevents.domain.event.DomainEvent;

public record IncidentAssigned(UUID incidentId, UUID assigneeId, Instant occurredAt) implements

DomainEvent {
    @Override public String eventType () {
        return "IncidentAssigned";
    }
}
