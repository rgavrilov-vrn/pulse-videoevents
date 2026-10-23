package ru.agroeco.pulse.videoevents.adapter.persistence;
import ru.agroeco.pulse.videoevents.domain.EvidenceRole;
import jakarta.persistence.*;
import lombok.*;
import java.time.Instant;
import java.util.UUID;
@Entity @Table(name="incident_evidence")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class EvidenceEntity{
  @Id private UUID id;
  @Column(name="incident_id",nullable=false) private UUID incidentId;
  @Column(name="media_asset_id",nullable=false) private UUID mediaAssetId;
  @Enumerated(EnumType.STRING) @Column(name="evidence_role",nullable=false) private EvidenceRole role;
  @Column(name="file_name",nullable=false) private String fileName;
  @Column(nullable=false,length=64) private String sha256;
  @Column(name="captured_at") private Instant capturedAt;
  @Column(name="created_at",nullable=false,updatable=false) private Instant createdAt;
  @PrePersist void prePersist(){if(id==null)id=UUID.randomUUID();if(createdAt==null)createdAt=Instant.now();}
}
