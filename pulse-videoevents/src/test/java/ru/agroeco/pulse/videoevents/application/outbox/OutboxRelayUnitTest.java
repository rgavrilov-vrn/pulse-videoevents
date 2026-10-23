package ru.agroeco.pulse.videoevents.application.outbox;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;
import ru.agroeco.pulse.videoevents.domain.event.DomainEvent;
import ru.agroeco.pulse.videoevents.domain.event.IncidentCreated;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OutboxRelayUnitTest{
  @Mock OutboxEventRepository repository;
  @Mock KafkaTemplate<String,DomainEvent> kafkaTemplate;
  @InjectMocks OutboxRelay relay;

  @Test
  void publishesNewOutboxEventAndMarksSent(){
    var event=new IncidentCreated(UUID.randomUUID(),"evt-1",Instant.now());
    var outbox=OutboxEventEntity.builder().id(UUID.randomUUID()).eventType("IncidentCreated")
      .incidentId(event.incidentId()).payload("{\"@type\":\"ru.agroeco.pulse.videoevents.domain.event.IncidentCreated\",\"incidentId\":\""+event.incidentId()+"\",\"externalEventId\":\"evt-1\",\"occurredAt\":\"2026-10-23T08:00:00Z\"}")
      .status(OutboxStatus.NEW).attempts(0).nextAttemptAt(Instant.now()).build();
    when(repository.findTop50ByStatusAndNextAttemptAtLessThanEqualOrderByCreatedAtAsc(eq(OutboxStatus.NEW),any()))
      .thenReturn(List.of(outbox));

    relay.publishPendingEvents();

    verify(kafkaTemplate).send(anyString(),eq(event.incidentId().toString()),any(DomainEvent.class));
    assertThat(outbox.getStatus()).isEqualTo(OutboxStatus.SENT);
    assertThat(outbox.getPublishedAt()).isNotNull();
  }

  @Test
  void schedulesRetryOnKafkaFailure(){
    var outbox=OutboxEventEntity.builder().id(UUID.randomUUID()).eventType("IncidentCreated")
      .incidentId(UUID.randomUUID()).payload("invalid").status(OutboxStatus.NEW).attempts(0)
      .nextAttemptAt(Instant.now()).build();
    when(repository.findTop50ByStatusAndNextAttemptAtLessThanEqualOrderByCreatedAtAsc(eq(OutboxStatus.NEW),any()))
      .thenReturn(List.of(outbox));

    relay.publishPendingEvents();

    assertThat(outbox.getStatus()).isEqualTo(OutboxStatus.NEW);
    assertThat(outbox.getAttempts()).isEqualTo(1);
    assertThat(outbox.getNextAttemptAt()).isAfter(Instant.now());
  }
}
