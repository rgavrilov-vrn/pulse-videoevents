package ru.agroeco.pulse.videoevents.application.outbox;
import org.springframework.data.jpa.repository.JpaRepository;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
public interface OutboxEventRepository extends JpaRepository<OutboxEventEntity,UUID>{
  List<OutboxEventEntity> findTop50ByStatusAndNextAttemptAtLessThanEqualOrderByCreatedAtAsc(
    OutboxStatus status, Instant nextAttemptAt);
}
