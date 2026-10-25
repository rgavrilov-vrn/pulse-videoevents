package ru.agroeco.pulse.videoevents.application;

import ru.agroeco.pulse.videoevents.adapter.persistence.AuditEventRepository;
import ru.agroeco.pulse.videoevents.application.dto.VideoEventRequest;
import ru.agroeco.pulse.videoevents.application.outbox.OutboxEventRepository;
import ru.agroeco.pulse.videoevents.application.outbox.OutboxStatus;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class EventDispatcherIntegrationTest {
    @Autowired
    IncidentService service;
    @Autowired
    AuditEventRepository audit;
    @Autowired
    OutboxEventRepository outbox;

    @Test
    void savesAuditAndOutboxEvent() {
        var r = new VideoEventRequest("evt-" + UUID.randomUUID(), Instant.now(), "MOTION_DETECTED", UUID.randomUUID().toString(), null, null, null);
        var incident = service.ingestVideoEvent(r);
        assertTrue(audit.findAll().stream().anyMatch(a -> incident.id().equals(a.getIncidentId())));
        assertTrue(outbox.findAll().stream().anyMatch(o -> incident.id().equals(o.getIncidentId()) && o.getStatus() == OutboxStatus.NEW));
    }
}
