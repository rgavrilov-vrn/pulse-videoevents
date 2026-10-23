package ru.agroeco.pulse.videoevents.adapter.persistence;
import jakarta.persistence.*;
import lombok.*;
import java.time.Instant;
import java.util.UUID;
@Entity @Table(name="video_server_events")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class VideoServerEventEntity{
  @Id private UUID id;
  @Column(name="external_event_id",nullable=false,unique=true) private String externalEventId;
  @Column(name="event_timestamp",nullable=false) private Instant eventTimestamp;
  @Column(name="event_type",nullable=false) private String eventType;
  @Column(name="object_external_id",nullable=false) private String objectExternalId;
  @Column(name="camera_external_id") private String cameraExternalId;
  @Column(name="server_address") private String serverAddress;
  @Column(name="raw_payload",nullable=false,columnDefinition="jsonb") private String rawPayload;
  @Column(name="incident_id") private UUID incidentId;
  @Column(name="received_at",nullable=false,updatable=false) private Instant receivedAt;
  @Column(name="processed_at") private Instant processedAt;
  @PrePersist void prePersist(){if(id==null)id=UUID.randomUUID();if(receivedAt==null)receivedAt=Instant.now();}
}
