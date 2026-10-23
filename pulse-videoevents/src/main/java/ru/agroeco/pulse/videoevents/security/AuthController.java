package ru.agroeco.pulse.videoevents.security;

import ru.agroeco.pulse.videoevents.application.dto.TokenRequest;
import ru.agroeco.pulse.videoevents.application.dto.TokenResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication")
public class AuthController {
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;

    @PostMapping("/token")
    @Operation(summary = "Issue JWT access token")
    public ResponseEntity<TokenResponse> token(@Valid @RequestBody TokenRequest request) {
        var authentication = authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(request.username(), request.password()));
        UserDetails user = (UserDetails) authentication.getPrincipal();
        return ResponseEntity.ok(new TokenResponse(
            jwtService.generateToken(user), "Bearer", jwtService.expirationSeconds()));
    }
}
