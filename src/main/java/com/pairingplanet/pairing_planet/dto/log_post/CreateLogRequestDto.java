package com.pairingplanet.pairing_planet.dto.log_post;

import java.util.List;
import java.util.UUID;

public record CreateLogRequestDto(
        UUID recipePublicId,
        String title,
        String content,
        Integer rating,
        List<UUID> imagePublicIds // [수정] String imageUrls -> UUID imagePublicIds
) {}