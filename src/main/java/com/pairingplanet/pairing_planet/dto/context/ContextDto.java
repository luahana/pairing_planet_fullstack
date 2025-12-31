package com.pairingplanet.pairing_planet.dto.context;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import java.util.Map;
import java.util.UUID;

public class ContextDto {

    public record DimensionRequest(@NotBlank String name) {}

    public record DimensionResponse(UUID publicId, String name) {}

    public record TagRequest(
            @NotBlank String tagName,
            @NotEmpty Map<String, String> displayNames, // 로케일별 이름 맵
            @NotEmpty Map<String, Integer> displayOrders // 로케일별 순서 맵
    ) {}

    public record TagResponse(
            UUID publicId,
            String tagName,
            String displayName, // 요청한 로케일에 맞는 이름
            Integer displayOrder, // 요청한 로케일에 맞는 순서
            UUID dimensionPublicId
    ) {}
}