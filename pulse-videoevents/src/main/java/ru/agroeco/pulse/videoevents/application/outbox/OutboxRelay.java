package ru.agroeco.pulse.videoevents.application.outbox;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import java.time.Instant;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/** At-least-once relay. A bounded batch stays locked until Kafka ACKs and DB commit. */
@Component
@Slf4j
@ConditionalOnProperty(name = "app.outbox.relay-enabled", havingValue = "true")
public class OutboxRelay {
    private final JdbcTemplate jdbc;
    private final KafkaTemplate<String, String> kafka;
    private final ObjectMapper mapper;
    private final String topic;
    private final int batchSize;
    private final int maxAttempts;
    private final long timeoutSeconds;

    public OutboxRelay(JdbcTemplate jdbc, KafkaTemplate<String, String> kafka, ObjectMapper mapper,
            @Value("${app.kafka.topic.incident-events}") String topic,
            @Value("${app.outbox.batch-size:50}") int batchSize,
            @Value("${app.outbox.max-attempts:5}") int maxAttempts,
            @Value("${app.outbox.send-timeout-seconds:10}") long timeoutSeconds) {
        if (batchSize < 1 || batchSize > 500 || maxAttempts < 1 || timeoutSeconds < 1) {
            throw new IllegalArgumentException("Invalid outbox configuration");
        }
        this.jdbc = jdbc; this.kafka = kafka; this.mapper = mapper; this.topic = topic;
        this.batchSize = batchSize; this.maxAttempts = maxAttempts; this.timeoutSeconds = timeoutSeconds;
    }

    @Scheduled(fixedDelayString = "${app.outbox.poll-delay-ms:5000}")
    @Transactional
    public void publishPendingEvents() {
        var rows = jdbc.query("SELECT id, incident_id, event_type, payload::text AS payload, created_at "
                + "FROM outbox_events WHERE status = 'NEW' AND next_attempt_at <= now() "
                + "ORDER BY created_at, id LIMIT ? FOR UPDATE SKIP LOCKED",
            (rs, row) -> new Pending(rs.getObject("id", UUID.class),
                rs.getObject("incident_id", UUID.class), rs.getString("event_type"),
                rs.getString("payload"), rs.getTimestamp("created_at").toInstant()), batchSize);
        for (Pending row : rows) {
            try {
                ObjectNode envelope = mapper.createObjectNode();
                envelope.put("eventId", row.id().toString());
                envelope.put("aggregateId", row.incidentId().toString());
                envelope.put("eventType", row.type());
                envelope.put("schemaVersion", 1);
                envelope.put("recordedAt", row.createdAt().toString());
                envelope.set("payload", mapper.readTree(row.payload()));
                var message = MessageBuilder.withPayload(mapper.writeValueAsString(envelope))
                    .setHeader(KafkaHeaders.TOPIC, topic)
                    .setHeader(KafkaHeaders.KEY, row.incidentId().toString()).build();
                kafka.send(message).get(timeoutSeconds, TimeUnit.SECONDS);
                jdbc.update("UPDATE outbox_events SET status = 'SENT', published_at = now(), "
                    + "last_error = NULL WHERE id = ?", row.id());
                log.info("Kafka acknowledged outbox event id={}, aggregateId={}", row.id(), row.incidentId());
            } catch (Exception ex) {
                if (ex instanceof InterruptedException) {
                    Thread.currentThread().interrupt();
                    throw new IllegalStateException("Outbox relay interrupted; batch will roll back", ex);
                }
                String error = ex.getClass().getSimpleName();
                jdbc.update("UPDATE outbox_events SET attempts = attempts + 1, "
                    + "status = CASE WHEN attempts + 1 >= ? THEN 'FAILED' ELSE 'NEW' END, "
                    + "next_attempt_at = now() + (power(2, least(attempts + 1, 8)) * interval '1 second'), "
                    + "last_error = ? WHERE id = ?", maxAttempts, error, row.id());
                log.warn("Outbox publication failed; persisted retry or FAILED status for id={}", row.id(), ex);
            }
        }
    }

    private record Pending(UUID id, UUID incidentId, String type, String payload, Instant createdAt) {}
}
