package ru.agroeco.pulse.videoevents.security;

import jakarta.persistence.*;
import lombok.*;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Entity @Table(name = "security_users")
@Getter @Setter @NoArgsConstructor
public class SecurityUser {
    @Id private UUID id;
    @Column(nullable = false, unique = true, length = 100) private String username;
    @Column(name = "password_hash", nullable = false) private String passwordHash;
    @Column(nullable = false) private boolean enabled = true;
    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(name = "security_user_roles", joinColumns = @JoinColumn(name = "user_id"))
    @Column(name = "role", nullable = false)
    @Enumerated(EnumType.STRING)
    private Set<AppRole> roles = new HashSet<>();
    @PrePersist void prePersist() { if (id == null) id = UUID.randomUUID(); }
}
