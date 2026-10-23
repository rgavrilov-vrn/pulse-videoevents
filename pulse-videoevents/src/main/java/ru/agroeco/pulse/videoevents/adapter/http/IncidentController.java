package ru.agroeco.pulse.videoevents.adapter.http;
import ru.agroeco.pulse.videoevents.application.IncidentService;
import ru.agroeco.pulse.videoevents.application.dto.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.UUID;
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@Tag(name="Incidents")
public class IncidentController{
  private final IncidentService service;
  @PostMapping("/video-events") @ResponseStatus(HttpStatus.CREATED)
  public IncidentResponse ingest(@Valid @RequestBody VideoEventRequest r){return service.ingestVideoEvent(r);}
  @GetMapping("/incidents") public List<IncidentResponse> all(){return service.findAll();}
  @GetMapping("/incidents/{id}") public IncidentResponse one(@PathVariable UUID id){return service.findById(id);}
  @PostMapping("/incidents/{id}/evidence") @ResponseStatus(HttpStatus.CREATED)
  public IncidentResponse evidence(@PathVariable UUID id,@Valid @RequestBody EvidenceRequest r){return service.attachEvidence(id,r);}
  @PostMapping("/incidents/{id}/assignment") public IncidentResponse assign(@PathVariable UUID id,@Valid @RequestBody AssignRequest r){return service.assign(id,r);}
  @PostMapping("/incidents/{id}/confirmation") public IncidentResponse confirm(@PathVariable UUID id){return service.confirm(id);}
  @PostMapping("/incidents/{id}/rejection") public IncidentResponse reject(@PathVariable UUID id){return service.reject(id);}
  @PostMapping("/incidents/{id}/closure") public IncidentResponse close(@PathVariable UUID id,@Valid @RequestBody CloseRequest r){return service.close(id,r);}
}
