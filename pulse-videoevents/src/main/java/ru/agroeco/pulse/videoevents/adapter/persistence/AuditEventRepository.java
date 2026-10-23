package ru.agroeco.pulse.videoevents.adapter.persistence;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.*;
public interface AuditEventRepository extends JpaRepository<AuditEventEntity,UUID>{}
