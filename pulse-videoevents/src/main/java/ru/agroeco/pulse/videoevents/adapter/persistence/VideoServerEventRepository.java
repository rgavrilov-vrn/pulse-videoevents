package ru.agroeco.pulse.videoevents.adapter.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface VideoServerEventRepository extends JpaRepository<VideoServerEventEntity, UUID> {
    Optional<VideoServerEventEntity> findByExternalEventId(String externalEventId);
}
