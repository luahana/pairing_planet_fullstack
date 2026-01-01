package com.pairingplanet.pairing_planet.dto.post.recipe;

import com.pairingplanet.pairing_planet.domain.enums.RecipeDifficulty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

public record RecipeRequestDto(
        @NotBlank String recipeTitle,
        String description,
        @NotBlank String editSummary, // 버전 관리용 수정 요약
        List<String> imageUrls,
        @NotEmpty List<IngredientRequestDto> ingredients,
        @NotEmpty List<StepRequestDto> steps,
        Integer cookingTime,
        RecipeDifficulty difficulty
) {}