package ru.agroeco.pulse.videoevents.security;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.util.Set;

@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "app.security.bootstrap.enabled", havingValue = "true")
public class SecurityBootstrap implements ApplicationRunner {
    private final SecurityUserRepository users;
    private final PasswordEncoder encoder;
    @Value("${app.security.bootstrap.username:}")
    private String username;
    @Value("${app.security.bootstrap.password:}")
    private String password;

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        if (username.isBlank() || password.length() < 16
                || password.getBytes(StandardCharsets.UTF_8).length > 72) {
            throw new IllegalStateException("Bootstrap requires username and a 16+ character password of at most 72 UTF-8 bytes");
        }
        if (users.findByUsername(username).isPresent()) return;
        var user = new SecurityUser();
        user.setUsername(username);
        user.setPasswordHash(encoder.encode(password));
        user.setRoles(Set.of(AppRole.ADMIN));
        users.save(user);
    }
}
