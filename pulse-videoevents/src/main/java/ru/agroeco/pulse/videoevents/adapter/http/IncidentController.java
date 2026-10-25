package ru.agroeco.pulse.videoevents.adapter.http;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import ru.agroeco.pulse.videoevents.application.IncidentService;
import ru.agroeco.pulse.videoevents.application.dto.*;

import ru.agroeco.pulse.videoevents.security.IncidentAuthorization;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@Tag(name = "Incidents")
public class IncidentController {
    private final IncidentService service;
    private final IncidentAuthorization access;

    @PostMapping("/video-events")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAuthority('video-event:ingest')")
    public IncidentResponse ingest(@Valid @RequestBody VideoEventRequest r) {
        return service.ingestVideoEvent(r);
    }

    @GetMapping("/incidents")
    @PreAuthorize("hasAnyAuthority('incident:read', 'incident:read:assigned')")
    public List<IncidentResponse> all(Authentication auth) {
        return service.findAll().stream().filter(i -> access.canRead(auth, i)).toList();
    }

    @GetMapping("/incidents/{id}")
    @PreAuthorize("hasAnyAuthority('incident:read', 'incident:read:assigned')")
    public IncidentResponse one(@PathVariable UUID id, Authentication auth) {
        var incident = service.findById(id);
        if (!access.canRead(auth, incident)) throw new AccessDeniedException("Access denied");
        return incident;
    }

    @PostMapping("/incidents/{id}/evidence")
    @ResponseStatus(HttpStatus.CREATED)
    @Transactional
    @PreAuthorize("hasAnyAuthority('incident:evidence', 'incident:evidence:assigned')")
    public IncidentResponse evidence(@PathVariable UUID id, @Valid @RequestBody EvidenceRequest r, Authentication auth) {
        access.requireMutation(auth, id, "incident:evidence");
        return service.attachEvidence(id, r);
    }

    @PostMapping("/incidents/{id}/assignment")
    @Transactional
    @PreAuthorize("hasAuthority('incident:assign')")
    public IncidentResponse assign(@PathVariable UUID id, @Valid @RequestBody AssignRequest r, Authentication auth) {
        access.requireMutation(auth, id, "incident:assign");
        return service.assign(id, r);
    }

    @PostMapping("/incidents/{id}/confirmation")
    @Transactional
    @PreAuthorize("hasAuthority('incident:confirm')")
    public IncidentResponse confirm(@PathVariable UUID id, Authentication auth) {
        access.requireMutation(auth, id, "incident:confirm");
        return service.confirm(id);
    }

    @PostMapping("/incidents/{id}/rejection")
    @Transactional
    @PreAuthorize("hasAuthority('incident:reject')")
    public IncidentResponse reject(@PathVariable UUID id, Authentication auth) {
        access.requireMutation(auth, id, "incident:reject");
        return service.reject(id);
    }

    @PostMapping("/incidents/{id}/closure")
    @Transactional
    @PreAuthorize("hasAnyAuthority('incident:close', 'incident:close:assigned')")
    public IncidentResponse close(@PathVariable UUID id, @Valid @RequestBody CloseRequest r, Authentication auth) {
        access.requireMutation(auth, id, "incident:close");
        return service.close(id, r);
    }
}
