package ru.agroeco.pulse.videoevents.domain.event;
import java.time.Instant;
import java.util.UUID;
public record IncidentCreated(UUID incidentId,String externalEventId,Instant occurredAt) implements DomainEvent{public String eventType(){return "IncidentCreated";}}
public record IncidentAssigned(UUID incidentId,UUID assigneeId,Instant occurredAt) implements DomainEvent{public String eventType(){return "IncidentAssigned";}}
public record IncidentEvidenceAttached(UUID incidentId,UUID mediaAssetId,Instant occurredAt) implements DomainEvent{public String eventType(){return "IncidentEvidenceAttached";}}
public record IncidentConfirmed(UUID incidentId,Instant occurredAt) implements DomainEvent{public String eventType(){return "IncidentConfirmed";}}
public record IncidentRejected(UUID incidentId,Instant occurredAt) implements DomainEvent{public String eventType(){return "IncidentRejected";}}
public record IncidentClosed(UUID incidentId,String resolution,Instant occurredAt) implements DomainEvent{public String eventType(){return "IncidentClosed";}}
