package com.pairingplanet.pairing_planet.dto.recipe;

import java.util.List;
import java.util.UUID;

public record CreateLogRequestDto(
        UUID recipePublicId,
        String title,
        String content,
        Integer rating,
        List<String> imageUrls
) {}