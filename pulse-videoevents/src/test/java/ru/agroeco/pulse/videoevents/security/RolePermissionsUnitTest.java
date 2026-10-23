package ru.agroeco.pulse.videoevents.security;

import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import ru.agroeco.pulse.videoevents.application.dto.IncidentResponse;
import ru.agroeco.pulse.videoevents.domain.IncidentStatus;
import ru.agroeco.pulse.videoevents.domain.Severity;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

class RolePermissionsUnitTest {
    @Test void operatorCannotReviewAndIntegrationCannotRead() {
        assertThat(AppRole.OPERATOR.permissions()).doesNotContain("incident:confirm", "user:manage");
        assertThat(AppRole.INTEGRATION.permissions()).containsExactly("video-event:ingest");
        assertThat(AppRole.ADMIN.permissions()).contains("incident:confirm", "user:manage");
    }
    @Test void responsibleCanReadOnlyAssignedIncident() {
        UUID userId = UUID.randomUUID();
        var principal = new AppPrincipal(userId, "responsible", "unused",
            List.<GrantedAuthority>of(new SimpleGrantedAuthority("incident:read:assigned")));
        var auth = new org.springframework.security.authentication.UsernamePasswordAuthenticationToken(
            principal, null, principal.getAuthorities());
        var policy = new IncidentAuthorization(mock(EntityManager.class));
        assertThat(policy.canRead(auth, incident(userId))).isTrue();
        assertThat(policy.canRead(auth, incident(UUID.randomUUID()))).isFalse();
        assertThat(policy.canRead(auth, incident(null))).isFalse();
    }
    private IncidentResponse incident(UUID assignee) {
        return new IncidentResponse(UUID.randomUUID(), "INC-1", "evt", IncidentStatus.RECORDED,
            Severity.MEDIUM, Instant.now(), null, null, null, assignee, null, null, 1);
    }
}
