package ru.agroeco.pulse.videoevents.application.outbox;
import com.fasterxml.jackson.databind.ObjectMapper;
import ru.agroeco.pulse.videoevents.domain.event.DomainEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;
import java.time.Instant;
@Component
@RequiredArgsConstructor
@Slf4j
public class OutboxListener{
  private final OutboxEventRepository repository;
  private final ObjectMapper objectMapper;
  @TransactionalEventListener(phase=TransactionPhase.BEFORE_COMMIT)
  public void on(DomainEvent event){
    try{
      repository.save(OutboxEventEntity.builder()
        .eventType(event.eventType())
        .incidentId(event.incidentId())
        .payload(objectMapper.writeValueAsString(event))
        .status(OutboxStatus.NEW)
        .attempts(0)
        .nextAttemptAt(Instant.now())
        .build());
      log.debug("Outbox event saved: type={}, incidentId={}",event.eventType(),event.incidentId());
    }catch(Exception e){
      log.error("Failed to save outbox event",e);
      throw new IllegalStateException("Outbox persistence failed",e);
    }
  }
}
