package ru.agroeco.pulse.videoevents.application.outbox;

import ru.agroeco.pulse.videoevents.application.JsonCodec;
import ru.agroeco.pulse.videoevents.domain.event.DomainEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class OutboxListener {
    private final JdbcTemplate jdbc;
    @TransactionalEventListener(phase = TransactionPhase.BEFORE_COMMIT)
    public void on(DomainEvent event) {
        // JdbcTemplate participates in the same datasource transaction as JPA.
        jdbc.update("INSERT INTO outbox_events (id, event_type, incident_id, payload, status, "
            + "attempts, next_attempt_at, created_at) VALUES (?, ?, ?, cast(? as jsonb), 'NEW', 0, now(), now())",
            UUID.randomUUID(), event.eventType(), event.incidentId(), JsonCodec.write(event));
    }
}
