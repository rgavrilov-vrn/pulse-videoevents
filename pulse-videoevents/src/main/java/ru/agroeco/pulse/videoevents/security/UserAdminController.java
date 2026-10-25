package ru.agroeco.pulse.videoevents.security;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.nio.charset.StandardCharsets;
import java.util.Set;
import java.util.UUID;

@RestController
@RequestMapping("/api/admin/users")
@RequiredArgsConstructor
@PreAuthorize("hasAuthority('user:manage')")
public class UserAdminController {
    private final SecurityUserRepository users;
    private final PasswordEncoder encoder;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Transactional
    public UserView create(@Valid @RequestBody CreateUser r) {
        if (r.password().getBytes(StandardCharsets.UTF_8).length > 72)
            throw new IllegalArgumentException("Password exceeds BCrypt byte limit");
        var user = new SecurityUser();
        user.setUsername(r.username());
        user.setPasswordHash(encoder.encode(r.password()));
        user.setRoles(Set.copyOf(r.roles()));
        user = users.saveAndFlush(user);
        return view(user);
    }

    @PutMapping("/{id}/roles")
    @Transactional
    public UserView roles(@PathVariable UUID id, @Valid @RequestBody ChangeRoles r, Authentication auth) {
        if (auth.getPrincipal() instanceof AppPrincipal p && p.id().equals(id))
            throw new IllegalArgumentException("Self role changes are not allowed");
        var user = users.findById(id).orElseThrow(() ->
                new ru.agroeco.pulse.videoevents.domain.exception.NotFoundException("User not found"));
        user.setRoles(Set.copyOf(r.roles()));
        return view(users.saveAndFlush(user));
    }

    private UserView view(SecurityUser u) {
        return new UserView(u.getId(), u.getUsername(), u.isEnabled(), Set.copyOf(u.getRoles()));
    }

    public record CreateUser(@NotBlank @Size(max = 100) String username,
                             @NotBlank @Size(min = 16, max = 72) String password, @NotEmpty Set<@NotNull AppRole> roles) {
    }

    public record ChangeRoles(@NotEmpty Set<@NotNull AppRole> roles) {
    }

    public record UserView(UUID id, String username, boolean enabled, Set<AppRole> roles) {
    }
}
