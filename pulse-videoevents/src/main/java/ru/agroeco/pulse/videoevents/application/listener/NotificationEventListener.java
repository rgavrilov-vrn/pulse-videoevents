package ru.agroeco.pulse.videoevents.application.listener;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;
import ru.agroeco.pulse.videoevents.domain.event.*;

@Component
@Slf4j
public class NotificationEventListener {
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void on(IncidentCreated e) {
        log.info("Notification: new incident {}", e.incidentId());
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void on(IncidentConfirmed e) {
        log.info("Notification: incident {} confirmed", e.incidentId());
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void on(IncidentClosed e) {
        log.info("Notification: incident {} closed", e.incidentId());
    }
}
