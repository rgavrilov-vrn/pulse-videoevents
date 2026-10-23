package ru.agroeco.pulse.videoevents.application;
import ru.agroeco.pulse.videoevents.adapter.persistence.*;
import ru.agroeco.pulse.videoevents.application.dto.*;
import ru.agroeco.pulse.videoevents.application.event.EventDispatcher;
import ru.agroeco.pulse.videoevents.domain.IncidentStatus;
import ru.agroeco.pulse.videoevents.domain.Severity;
import ru.agroeco.pulse.videoevents.domain.event.*;
import ru.agroeco.pulse.videoevents.domain.exception.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;

@Service @RequiredArgsConstructor @Slf4j
public class IncidentService{
  private final IncidentRepository incidents;
  private final EvidenceRepository evidence;
  private final VideoServerEventRepository videoEvents;
  private final EventDispatcher events;
  private final AtomicLong sequence=new AtomicLong(1);

  @Transactional
  public IncidentResponse ingestVideoEvent(VideoEventRequest r){
    var existing=incidents.findByExternalEventId(r.externalEventId());
    if(existing.isPresent()){log.info("Duplicate video event ignored: {}",r.externalEventId());return toResponse(existing.get());}
    Instant eventTime=r.eventTimestamp()==null?Instant.now():r.eventTimestamp();
    var e=VideoServerEventEntity.builder().externalEventId(r.externalEventId()).eventTimestamp(eventTime)
      .eventType(r.eventType()).objectExternalId(r.objectExternalId()).cameraExternalId(r.cameraExternalId())
      .serverAddress(r.serverAddress()).rawPayload(r.payload()==null?"{}":r.payload().toString()).build();
    videoEvents.save(e);
    var i=IncidentEntity.builder().incidentNumber("INC-%d-%06d".formatted(LocalDate.now().getYear(),sequence.getAndIncrement()))
      .externalEventId(r.externalEventId()).status(IncidentStatus.RECORDED).severity(Severity.MEDIUM)
      .eventTime(eventTime).objectId(UUID.nameUUIDFromBytes(r.objectExternalId().getBytes()))
      .cameraId(r.cameraExternalId()==null?null:UUID.nameUUIDFromBytes(r.cameraExternalId().getBytes())).build();
    i=incidents.save(i);
    e.setIncidentId(i.getId()); e.setProcessedAt(Instant.now()); videoEvents.save(e);
    events.dispatch(new IncidentCreated(i.getId(),r.externalEventId(),Instant.now()));
    log.info("Incident created: id={}, number={}",i.getId(),i.getIncidentNumber());
    return toResponse(i);
  }
  @Transactional(readOnly=true) public List<IncidentResponse> findAll(){return incidents.findAll().stream().map(this::toResponse).toList();}
  @Transactional(readOnly=true) public IncidentResponse findById(UUID id){return toResponse(load(id));}
  @Transactional
  public IncidentResponse attachEvidence(UUID id,EvidenceRequest r){
    var i=load(id); ensureOpen(i);
    if(evidence.existsByIncidentIdAndMediaAssetId(id,r.mediaAssetId()))throw new BusinessRuleException("Evidence already attached");
    evidence.save(EvidenceEntity.builder().incidentId(id).mediaAssetId(r.mediaAssetId()).role(r.role())
      .fileName(r.fileName()).sha256(r.sha256()).capturedAt(r.capturedAt()).build());
    events.dispatch(new IncidentEvidenceAttached(id,r.mediaAssetId(),Instant.now()));
    return toResponse(i);
  }
  @Transactional public IncidentResponse assign(UUID id,AssignRequest r){
    var i=load(id); ensureOpen(i);
    if(r.dueAt()!=null&&r.dueAt().isBefore(i.getEventTime()))throw new BusinessRuleException("Due date cannot be before event time");
    i.setAssigneeId(r.assigneeId()); i.setDueAt(r.dueAt());
    var saved=incidents.save(i);
    events.dispatch(new IncidentAssigned(id,r.assigneeId(),Instant.now()));
    return toResponse(saved);
  }
  @Transactional public IncidentResponse confirm(UUID id){var i=load(id);transition(i,IncidentStatus.CONFIRMED);var s=incidents.save(i);events.dispatch(new IncidentConfirmed(id,Instant.now()));return toResponse(s);}
  @Transactional public IncidentResponse reject(UUID id){var i=load(id);transition(i,IncidentStatus.REJECTED);var s=incidents.save(i);events.dispatch(new IncidentRejected(id,Instant.now()));return toResponse(s);}
  @Transactional public IncidentResponse close(UUID id,CloseRequest r){var i=load(id);transition(i,IncidentStatus.CLOSED);i.setResolutionComment(r.resolutionComment());var s=incidents.save(i);events.dispatch(new IncidentClosed(id,r.resolutionComment(),Instant.now()));return toResponse(s);}
  private void transition(IncidentEntity i,IncidentStatus t){
    ensureOpen(i);
    if(!i.getStatus().canTransitionTo(t))throw new BusinessRuleException("Cannot transition from %s to %s".formatted(i.getStatus(),t));
    i.setStatus(t);
  }
  private void ensureOpen(IncidentEntity i){if(i.getStatus()==IncidentStatus.CLOSED||i.getStatus()==IncidentStatus.CANCELLED)throw new BusinessRuleException("Incident is closed or cancelled");}
  private IncidentEntity load(UUID id){return incidents.findById(id).orElseThrow(()->new NotFoundException("Incident not found: "+id));}
  private IncidentResponse toResponse(IncidentEntity i){return new IncidentResponse(i.getId(),i.getIncidentNumber(),i.getExternalEventId(),i.getStatus(),i.getSeverity(),i.getEventTime(),i.getObjectId(),i.getCameraId(),i.getOperatorId(),i.getAssigneeId(),i.getDueAt(),i.getResolutionComment(),i.getVersion());}
}
