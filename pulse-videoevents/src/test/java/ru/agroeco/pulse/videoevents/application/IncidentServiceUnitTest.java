package ru.agroeco.pulse.videoevents.application;
import ru.agroeco.pulse.videoevents.adapter.persistence.*;
import ru.agroeco.pulse.videoevents.application.dto.*;
import ru.agroeco.pulse.videoevents.application.event.EventDispatcher;
import ru.agroeco.pulse.videoevents.domain.IncidentStatus;
import ru.agroeco.pulse.videoevents.domain.event.*;
import ru.agroeco.pulse.videoevents.domain.exception.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
@ExtendWith(MockitoExtension.class)
class IncidentServiceUnitTest{
  @Mock IncidentRepository incidentRepository;
  @Mock EvidenceRepository evidenceRepository;
  @Mock VideoServerEventRepository videoEventRepository;
  @Mock EventDispatcher eventDispatcher;
  @InjectMocks IncidentService service;
  private VideoEventRequest request(){return new VideoEventRequest("evt-1",Instant.parse("2026-10-23T08:00:00Z"),"MOTION_DETECTED","object-1","camera-1","192.168.1.10",null);}
  private IncidentEntity incident(IncidentStatus status){return IncidentEntity.builder().id(UUID.randomUUID()).incidentNumber("INC-2026-000001").externalEventId("evt-1").status(status).eventTime(Instant.parse("2026-10-23T08:00:00Z")).build();}
  @Test void ingestCreatesIncidentAndDispatchesEvent(){
    when(incidentRepository.findByExternalEventId("evt-1")).thenReturn(Optional.empty());
    when(incidentRepository.save(any(IncidentEntity.class))).thenAnswer(i->i.getArgument(0));
    var response=service.ingestVideoEvent(request());
    assertThat(response.status()).isEqualTo(IncidentStatus.RECORDED);
    ArgumentCaptor<DomainEvent> captor=ArgumentCaptor.forClass(DomainEvent.class);
    verify(eventDispatcher).dispatch(captor.capture());
    assertThat(captor.getValue()).isInstanceOf(IncidentCreated.class);
  }
  @Test void ingestReturnsExistingIncidentWithoutDispatch(){
    var existing=incident(IncidentStatus.RECORDED);
    when(incidentRepository.findByExternalEventId("evt-1")).thenReturn(Optional.of(existing));
    assertThat(service.ingestVideoEvent(request()).id()).isEqualTo(existing.getId());
    verify(eventDispatcher,never()).dispatch(any());
  }
  @Test void confirmTransitionsRecordedIncident(){
    var existing=incident(IncidentStatus.RECORDED);
    when(incidentRepository.findById(existing.getId())).thenReturn(Optional.of(existing));
    when(incidentRepository.save(any(IncidentEntity.class))).thenAnswer(i->i.getArgument(0));
    assertThat(service.confirm(existing.getId()).status()).isEqualTo(IncidentStatus.CONFIRMED);
    verify(eventDispatcher).dispatch(any(IncidentConfirmed.class));
  }
  @Test void closeFromRecordedIsRejected(){
    var existing=incident(IncidentStatus.RECORDED);
    when(incidentRepository.findById(existing.getId())).thenReturn(Optional.of(existing));
    assertThatThrownBy(()->service.close(existing.getId(),new CloseRequest("done"))).isInstanceOf(BusinessRuleException.class);
  }
  @Test void findByIdThrowsNotFound(){
    UUID id=UUID.randomUUID();
    when(incidentRepository.findById(id)).thenReturn(Optional.empty());
    assertThatThrownBy(()->service.findById(id)).isInstanceOf(NotFoundException.class);
  }
}
