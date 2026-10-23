package ru.agroeco.pulse.videoevents.application.listener;
import ru.agroeco.pulse.videoevents.adapter.persistence.AuditEventEntity;
import ru.agroeco.pulse.videoevents.adapter.persistence.AuditEventRepository;
import ru.agroeco.pulse.videoevents.domain.event.DomainEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;
@Component @RequiredArgsConstructor
public class AuditEventListener{
  private final AuditEventRepository repository;
  @TransactionalEventListener(phase=TransactionPhase.BEFORE_COMMIT)
  public void on(DomainEvent event){
    repository.save(AuditEventEntity.builder().eventType(event.eventType()).incidentId(event.incidentId())
      .payload(event.toString()).occurredAt(event.occurredAt()).build());
  }
}
