package ru.agroeco.pulse.videoevents.application.outbox;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.messaging.Message;
import java.lang.reflect.Constructor;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OutboxRelayUnitTest {
    @Mock JdbcTemplate jdbc;
    @Mock KafkaTemplate<String, String> kafka;
    private OutboxRelay relay;

    @BeforeEach void setup() {
        relay = new OutboxRelay(jdbc, kafka, new ObjectMapper(), "events", 7, 5, 1);
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private void pending() throws Exception {
        Class<?> type = Class.forName(OutboxRelay.class.getName() + "$Pending");
        Constructor<?> constructor = type.getDeclaredConstructors()[0];
        constructor.setAccessible(true);
        Object row = constructor.newInstance(UUID.randomUUID(), UUID.randomUUID(),
            "IncidentCreated", "{}", Instant.now());
        when(jdbc.query(anyString(), any(RowMapper.class), eq(7))).thenReturn((List) List.of(row));
    }

    @Test void marksSentOnlyAfterAcknowledgement() throws Exception {
        pending();
        when(kafka.send(any(Message.class))).thenReturn(CompletableFuture.completedFuture(null));
        relay.publishPendingEvents();
        verify(jdbc).update(contains("status = 'SENT'"), any(UUID.class));
        verify(jdbc, never()).update(contains("attempts = attempts + 1"), anyInt(), anyString(), any(UUID.class));
    }

    @Test void failedFutureSchedulesRetryWithoutMarkingSent() throws Exception {
        pending();
        when(kafka.send(any(Message.class))).thenReturn(
            CompletableFuture.<SendResult<String, String>>failedFuture(new IllegalStateException("broker down")));
        relay.publishPendingEvents();
        verify(jdbc, never()).update(contains("status = 'SENT'"), any(UUID.class));
        verify(jdbc).update(contains("attempts = attempts + 1"), eq(5), anyString(), any(UUID.class));
    }

    @Test void timeoutDoesNotMarkSent() throws Exception {
        pending();
        when(kafka.send(any(Message.class))).thenReturn(new CompletableFuture<>());
        relay.publishPendingEvents();
        verify(jdbc, never()).update(contains("status = 'SENT'"), any(UUID.class));
        verify(jdbc).update(contains("attempts = attempts + 1"), eq(5), anyString(), any(UUID.class));
    }
}
