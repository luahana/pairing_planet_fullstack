package com.pairingplanet.pairing_planet.dto.log_post;

import com.pairingplanet.pairing_planet.dto.recipe.RecipeSummaryDto;

import java.util.List;
import java.util.UUID;

public record LogPostDetailResponseDto(
        UUID publicId,
        String title,
        String content,
        Integer rating,
        List<String> imageUrls,
        RecipeSummaryDto linkedRecipe // 상단 "연결된 레시피 요약 카드"용
) {}