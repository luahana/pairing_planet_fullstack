package com.pairingplanet.pairing_planet.dto.post.recipe;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record RecipeDetailResponseDto(
        UUID publicId,
        UUID rootRecipePublicId,   // 오리지널 레시피 식별자
        UUID parentRecipePublicId, // 직전 부모 레시피 식별자
        int version,               // 현재 버전
        String title,
        String description,
        List<IngredientRequestDto> ingredients,
        List<StepRequestDto> steps,
        Integer cookingTime,
        String difficulty,
        String editSummary,
        Instant versionCreatedAt
) {}