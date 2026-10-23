package ru.agroeco.pulse.videoevents.security;

import jakarta.persistence.EntityManager;
import jakarta.persistence.LockModeType;
import lombok.RequiredArgsConstructor;
import ru.agroeco.pulse.videoevents.adapter.persistence.IncidentEntity;
import ru.agroeco.pulse.videoevents.application.dto.IncidentResponse;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Component @RequiredArgsConstructor
public class IncidentAuthorization {
    private final EntityManager entityManager;
    public boolean has(Authentication auth, String permission) {
        return auth != null && auth.isAuthenticated() && auth.getAuthorities().stream()
            .anyMatch(a -> permission.equals(a.getAuthority()));
    }
    public boolean canRead(Authentication auth, IncidentResponse incident) {
        return has(auth, "incident:read") || (has(auth, "incident:read:assigned")
            && auth.getPrincipal() instanceof AppPrincipal p && p.id().equals(incident.assigneeId()));
    }
    @Transactional(propagation = Propagation.MANDATORY)
    public void requireMutation(Authentication auth, java.util.UUID id, String permission) {
        boolean global = has(auth, permission);
        boolean assigned = has(auth, permission + ":assigned");
        if (!global && !assigned) throw new AccessDeniedException("Access denied");
        var incident = entityManager.find(IncidentEntity.class, id, LockModeType.PESSIMISTIC_WRITE);
        if (incident == null) {
            if (global) throw new ru.agroeco.pulse.videoevents.domain.exception.NotFoundException("Incident not found");
            throw new AccessDeniedException("Access denied");
        }
        if (!global && (!(auth.getPrincipal() instanceof AppPrincipal p)
                || !p.id().equals(incident.getAssigneeId()))) throw new AccessDeniedException("Access denied");
    }
}
