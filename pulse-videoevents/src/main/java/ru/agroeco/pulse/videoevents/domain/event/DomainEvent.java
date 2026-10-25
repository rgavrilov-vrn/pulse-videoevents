package ru.agroeco.pulse.videoevents.domain.event;

import java.time.Instant;
import java.util.UUID;

public interface DomainEvent {
    UUID incidentId();

    String eventType();

    Instant occurredAt();
}
