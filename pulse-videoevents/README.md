# pulse-videoevents

Spring Boot 3 API с PostgreSQL, Flyway, Spring Modulith, JWT, Swagger, Actuator/Prometheus, Kafka и Outbox pattern.

## Outbox pattern

1. `IncidentService` публикует `DomainEvent` через `SpringEventDispatcher`.
2. `OutboxListener` в той же транзакции сохраняет событие в `outbox_events` со статусом `NEW`.
3. `OutboxRelay` каждые 5 секунд читает `NEW` события и отправляет их в Kafka.
4. После успешной отправки запись получает `SENT`; при ошибке увеличивается `attempts` и планируется retry с exponential backoff.
5. После `max-attempts` запись становится `FAILED`.

Это гарантирует: если транзакция БД зафиксировалась, событие не будет потеряно; если Kafka недоступна, отправка будет повторена.

## Kafka

Topic: `pulse.videoevents.incident-events`.

События: `IncidentCreated`, `IncidentAssigned`, `IncidentEvidenceAttached`, `IncidentConfirmed`, `IncidentRejected`, `IncidentClosed`.

Kafka UI: http://localhost:8085

## Запуск

```bash
docker compose up --build
```

## JWT

```bash
curl -X POST http://localhost:8080/api/auth/token -H "Content-Type: application/json" -d '{"username":"operator","password":"operator"}'
```

## Мониторинг

- Prometheus: http://localhost:9090
- Grafana: http://localhost:3000
- Metrics: http://localhost:8080/actuator/prometheus

## Тесты

```bash
mvn test
```
