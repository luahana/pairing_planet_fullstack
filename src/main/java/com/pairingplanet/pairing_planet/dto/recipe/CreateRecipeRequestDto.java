package com.pairingplanet.pairing_planet.dto.recipe;

import com.pairingplanet.pairing_planet.domain.enums.RecipeDifficulty;
import com.pairingplanet.pairing_planet.dto.food.FoodRequestDto;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;
import java.util.UUID;

public record CreateRecipeRequestDto(
        String title,
        String description,
        String culinaryLocale,
        Long food1MasterId,
        List<IngredientDto> ingredients,
        List<StepDto> steps,
        List<UUID> imagePublicIds, // 대표 사진들
        String changeCategory,  // 변형 시 "무엇을 바꿨나요?"
        UUID parentPublicId     // 변형 대상 레시피 (NULL이면 오리지널)
) {}