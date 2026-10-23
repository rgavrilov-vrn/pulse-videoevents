package ru.agroeco.pulse.videoevents.adapter.persistence;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.*;
public interface IncidentRepository extends JpaRepository<IncidentEntity,UUID>{Optional<IncidentEntity> findByExternalEventId(String externalEventId);}
