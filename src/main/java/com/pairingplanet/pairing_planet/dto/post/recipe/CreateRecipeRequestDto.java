package com.pairingplanet.pairing_planet.dto.post.recipe;

import com.pairingplanet.pairing_planet.domain.enums.RecipeDifficulty;
import com.pairingplanet.pairing_planet.dto.food.FoodRequestDto;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;
import java.util.UUID;

public record CreateRecipeRequestDto(
        @NotNull(message = "Food1 is required")
        FoodRequestDto food1,

        FoodRequestDto food2,

        UUID whenContextId,
        UUID dietaryContextId,
        List<String> hashtags,
        Boolean isPrivate,
        Boolean commentsEnabled, // 댓글 허용 여부 추가

        @Size(max = 3, message = "Max 3 images allowed")
        List<String> imageUrls,
        @NotBlank String recipeTitle,
        String description,
        @NotBlank String editSummary, // 버전 관리용 수정 요약
        @NotEmpty List<IngredientRequestDto> ingredients,
        @NotEmpty List<StepRequestDto> steps,
        Integer cookingTime,
        RecipeDifficulty difficulty
) {}