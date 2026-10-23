package ru.agroeco.pulse.videoevents.application.listener;

import ru.agroeco.pulse.videoevents.application.JsonCodec;
import ru.agroeco.pulse.videoevents.domain.event.DomainEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;
import java.sql.Timestamp;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class AuditEventListener {
    private final JdbcTemplate jdbc;
    @TransactionalEventListener(phase = TransactionPhase.BEFORE_COMMIT)
    public void on(DomainEvent event) {
        jdbc.update("INSERT INTO audit_events (id, event_type, incident_id, payload, occurred_at) "
            + "VALUES (?, ?, ?, cast(? as jsonb), ?)", UUID.randomUUID(), event.eventType(),
            event.incidentId(), JsonCodec.write(event), Timestamp.from(event.occurredAt()));
    }
}
