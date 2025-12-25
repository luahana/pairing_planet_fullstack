package com.pairingplanet.pairing_planet.dto.context;

import jakarta.validation.constraints.NotBlank;
import java.util.UUID;

public class ContextDto {

    // --- Dimension DTOs ---
    public record DimensionRequest(
            @NotBlank String name
    ) {}

    public record DimensionResponse(
            UUID publicId,
            String name
    ) {}

    // --- Tag DTOs ---
    public record TagRequest(
            @NotBlank String tagName,
            @NotBlank String displayName,
            @NotBlank String locale,
            Integer displayOrder
    ) {}

    public record TagResponse(
            UUID publicId,
            String tagName,
            String displayName,
            String locale,
            Integer displayOrder,
            UUID dimensionPublicId // 어떤 디멘션에 속하는지
    ) {}
}