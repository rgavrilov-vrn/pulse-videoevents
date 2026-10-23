package ru.agroeco.pulse.videoevents.adapter.persistence;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.*;
public interface EvidenceRepository extends JpaRepository<EvidenceEntity,UUID>{boolean existsByIncidentIdAndMediaAssetId(UUID incidentId,UUID mediaAssetId);}
