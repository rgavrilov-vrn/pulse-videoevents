package ru.agroeco.pulse.videoevents.application.dto;

import jakarta.validation.constraints.*;
import ru.agroeco.pulse.videoevents.domain.EvidenceRole;

import java.time.Instant;
import java.util.UUID;

public record EvidenceRequest(@NotNull UUID mediaAssetId, @NotNull EvidenceRole role, @NotBlank String fileName,
                              @NotBlank @Pattern(regexp = "(?i)[0-9a-f]{64}") String sha256, Instant capturedAt) {
}
