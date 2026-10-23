package ru.agroeco.pulse.videoevents.adapter.http;
import ru.agroeco.pulse.videoevents.application.IncidentService;
import ru.agroeco.pulse.videoevents.application.dto.*;
import ru.agroeco.pulse.videoevents.domain.IncidentStatus;
import ru.agroeco.pulse.videoevents.domain.Severity;
import ru.agroeco.pulse.videoevents.domain.exception.BusinessRuleException;
import ru.agroeco.pulse.videoevents.domain.exception.NotFoundException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import java.time.Instant;
import java.util.UUID;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
@WebMvcTest(IncidentController.class)
@AutoConfigureMockMvc(addFilters=false)
@Import(ru.agroeco.pulse.videoevents.adapter.http.GlobalExceptionHandler.class)
class IncidentControllerTest{
  @Autowired MockMvc mockMvc;
  @MockBean IncidentService service;
  @Test void ingestReturnsCreatedIncident() throws Exception{
    UUID id=UUID.randomUUID();
    when(service.ingestVideoEvent(any(VideoEventRequest.class))).thenReturn(new IncidentResponse(id,"INC-2026-000001","evt-1",IncidentStatus.RECORDED,Severity.MEDIUM,Instant.now(),UUID.randomUUID(),null,null,null,null,null,1));
    mockMvc.perform(post("/api/video-events").contentType(MediaType.APPLICATION_JSON).content("""
      {"externalEventId":"evt-1","eventTimestamp":"2026-10-23T08:00:00Z","eventType":"MOTION_DETECTED","objectExternalId":"object-1"}
      """)).andExpect(status().isCreated()).andExpect(jsonPath("$.id").value(id.toString()));
  }
  @Test void ingestValidationReturnsBadRequest() throws Exception{
    mockMvc.perform(post("/api/video-events").contentType(MediaType.APPLICATION_JSON).content("{}"))
      .andExpect(status().isBadRequest()).andExpect(jsonPath("$.code").value("VALIDATION_ERROR"));
  }
  @Test void businessRuleReturnsConflict() throws Exception{
    UUID id=UUID.randomUUID();
    when(service.confirm(id)).thenThrow(new BusinessRuleException("Invalid transition"));
    mockMvc.perform(post("/api/incidents/{id}/confirmation",id)).andExpect(status().isConflict());
  }
  @Test void notFoundReturns404() throws Exception{
    UUID id=UUID.randomUUID();
    when(service.findById(id)).thenThrow(new NotFoundException("Incident not found"));
    mockMvc.perform(get("/api/incidents/{id}",id)).andExpect(status().isNotFound());
  }
}
