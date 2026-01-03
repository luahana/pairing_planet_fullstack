package com.pairingplanet.pairing_planet.dto.log_post;

import java.util.UUID;

public record LogPostSummaryDto(
        UUID publicId,
        String title,
        Integer rating,
        String thumbnail,
        String creatorName
) {}