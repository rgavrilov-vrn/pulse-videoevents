package ru.agroeco.pulse.videoevents.application.event;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
@Slf4j
@ConditionalOnProperty(name = "app.kafka.enabled", havingValue = "true")
public class IncidentEventKafkaConsumer {
    private final ObjectMapper mapper;
    private final JdbcTemplate jdbc;

    @KafkaListener(topics = "${app.kafka.topic.incident-events}",
        groupId = "${spring.kafka.consumer.group-id}",
        properties = {"value.deserializer=org.apache.kafka.common.serialization.StringDeserializer"})
    @Transactional
    public void on(String message) throws Exception {
        JsonNode event = mapper.readTree(message);
        UUID eventId = UUID.fromString(event.path("eventId").asText());
        UUID aggregateId = UUID.fromString(event.path("aggregateId").asText());
        String eventType = event.path("eventType").asText();
        if (event.path("schemaVersion").asInt() != 1 || eventType.isBlank() || !event.has("payload")) {
            throw new IllegalArgumentException("Invalid or unsupported event envelope");
        }
        int inserted = jdbc.update("INSERT INTO processed_events (consumer_name, event_id) "
            + "VALUES (?, ?) ON CONFLICT DO NOTHING", "incident-log-v1", eventId);
        if (inserted == 0) {
            log.debug("Skipping duplicate Kafka event id={}", eventId);
            return;
        }
        // Only a logging consumer today. Future DB side effects must share this transaction.
        // External side effects need their own durable delivery mechanism.
        log.info("Kafka event handled: id={}, type={}, aggregateId={}", eventId, eventType, aggregateId);
    }
}
