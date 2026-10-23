package ru.agroeco.pulse.videoevents.application.outbox;
import ru.agroeco.pulse.videoevents.domain.event.DomainEvent;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import java.time.Instant;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
@ConditionalOnProperty(name="app.outbox.relay-enabled",havingValue="true")
public class OutboxRelay{
  private final OutboxEventRepository repository;
  private final KafkaTemplate<String, DomainEvent> kafkaTemplate;
  private final ObjectMapper objectMapper;
  @Value("${app.kafka.topic.incident-events}") private String topic;
  @Value("${app.outbox.batch-size}") private int batchSize;
  @Value("${app.outbox.max-attempts}") private int maxAttempts;

  @Scheduled(fixedDelayString="${app.outbox.poll-delay-ms}")
  @Transactional
  public void publishPendingEvents(){
    List<OutboxEventEntity> events=repository
      .findTop50ByStatusAndNextAttemptAtLessThanEqualOrderByCreatedAtAsc(OutboxStatus.NEW,Instant.now());
    for(OutboxEventEntity outbox:events){
      try{
        DomainEvent event=objectMapper.readValue(outbox.getPayload(),DomainEvent.class);
        kafkaTemplate.send(topic,outbox.getIncidentId().toString(),event);
        outbox.setStatus(OutboxStatus.SENT);
        outbox.setPublishedAt(Instant.now());
        log.info("Outbox event published: type={}, incidentId={}",outbox.getEventType(),outbox.getIncidentId());
      }catch(Exception e){
        outbox.setAttempts(outbox.getAttempts()+1);
        outbox.setLastError(e.getMessage());
        outbox.setNextAttemptAt(Instant.now().plusSeconds((long)Math.pow(2,Math.min(outbox.getAttempts(),6))));
        if(outbox.getAttempts()>=maxAttempts){
          outbox.setStatus(OutboxStatus.FAILED);
          log.error("Outbox event failed permanently: id={}, attempts={}",outbox.getId(),outbox.getAttempts(),e);
        }else{
          log.warn("Outbox event retry scheduled: id={}, attempt={}",outbox.getId(),outbox.getAttempts(),e);
        }
      }
    }
    repository.saveAll(events);
  }
}
