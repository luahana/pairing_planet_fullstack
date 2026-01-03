package com.pairingplanet.pairing_planet.dto.recipe;

import java.util.UUID;

public record RecipeSummaryDto(
        UUID publicId,
        String title,
        String culinaryLocale,
        String creatorName,
        String thumbnail // display_order = 0인 이미지
) {}