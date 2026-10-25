package ru.agroeco.pulse.videoevents.application.dto;

import ru.agroeco.pulse.videoevents.domain.IncidentStatus;
import ru.agroeco.pulse.videoevents.domain.Severity;

import java.time.Instant;
import java.util.UUID;

public record IncidentResponse(UUID id, String incidentNumber, String externalEventId, IncidentStatus status, Severity severity,
                               Instant eventTime, UUID objectId, UUID cameraId, UUID operatorId, UUID assigneeId, Instant dueAt, String resolutionComment, long version) {
}
