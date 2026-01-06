package com.pairingplanet.pairing_planet.dto.log_post;

import com.pairingplanet.pairing_planet.dto.image.ImageResponseDto; // [추가]
import com.pairingplanet.pairing_planet.dto.recipe.RecipeSummaryDto;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

public record LogPostDetailResponseDto(
        UUID publicId,
        String title,
        String content,
        String outcome,  // SUCCESS, PARTIAL, FAILED
        List<ImageResponseDto> images, // [수정] List<String> -> List<ImageResponseDto>
        RecipeSummaryDto linkedRecipe,
        Instant createdAt
) {}