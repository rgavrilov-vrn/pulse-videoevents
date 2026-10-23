package ru.agroeco.pulse.videoevents.application.dto;
import jakarta.validation.constraints.NotBlank;
import java.time.Instant;
import java.util.Map;
public record VideoEventRequest(@NotBlank String externalEventId,Instant eventTimestamp,@NotBlank String eventType,
  @NotBlank String objectExternalId,String cameraExternalId,String serverAddress,Map<String,Object> payload){}
