package ru.agroeco.pulse.videoevents.security;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface SecurityUserRepository extends JpaRepository<SecurityUser, UUID> {
    @EntityGraph(attributePaths = "roles")
    Optional<SecurityUser> findByUsername(String username);
}
