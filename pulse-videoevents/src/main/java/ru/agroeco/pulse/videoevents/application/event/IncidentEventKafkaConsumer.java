package ru.agroeco.pulse.videoevents.application.event;
import ru.agroeco.pulse.videoevents.domain.event.DomainEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
@Component
@Slf4j
@ConditionalOnProperty(name="app.kafka.enabled",havingValue="true")
public class IncidentEventKafkaConsumer{
  @KafkaListener(topics="${app.kafka.topic.incident-events}",groupId="${spring.kafka.consumer.group-id}")
  public void on(DomainEvent event){
    log.info("Kafka event consumed: type={}, incidentId={}",event.eventType(),event.incidentId());
  }
}
