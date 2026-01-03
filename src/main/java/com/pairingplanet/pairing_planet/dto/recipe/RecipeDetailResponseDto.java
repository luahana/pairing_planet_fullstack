package com.pairingplanet.pairing_planet.dto.recipe;

import com.pairingplanet.pairing_planet.domain.entity.recipe.Recipe;
import com.pairingplanet.pairing_planet.dto.log_post.LogPostSummaryDto;
import lombok.Builder;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Builder
public record RecipeDetailResponseDto(
        UUID publicId,
        String title,
        String description,
        String culinaryLocale,
        String changeCategory,
        RecipeSummaryDto rootInfo,      // [원칙 1] 상단 고정 루트 레시피 정보
        RecipeSummaryDto parentInfo,    // Inspired by 정보
        List<IngredientDto> ingredients,
        List<StepDto> steps,
        List<String> imageUrls,
        List<RecipeSummaryDto> variants, // 하위 변형 리스트
        List<LogPostSummaryDto> logs    // 연결된 로그 리스트
) {
    // 💡 Entity -> DTO 변환을 위한 정적 팩토리 메서드
    public static RecipeDetailResponseDto from(Recipe recipe, List<RecipeSummaryDto> variants, List<LogPostSummaryDto> logs) {
        Recipe root = recipe.getRootRecipe();
        Recipe parent = recipe.getParentRecipe();

        return RecipeDetailResponseDto.builder()
                .publicId(recipe.getPublicId())
                .title(recipe.getTitle())
                .description(recipe.getDescription())
                .culinaryLocale(recipe.getCulinaryLocale())
                .changeCategory(recipe.getChangeCategory())
                .rootInfo(root != null ? new RecipeSummaryDto(root.getPublicId(), root.getTitle(), root.getCulinaryLocale(), null, null) : null)
                .parentInfo(parent != null ? new RecipeSummaryDto(parent.getPublicId(), parent.getTitle(), null, null, null) : null)
                .ingredients(recipe.getIngredients().stream().map(i -> new IngredientDto(i.getName(), i.getAmount(), i.getType())).toList())
                .steps(recipe.getSteps().stream().map(s -> new StepDto(s.getStepNumber(), s.getDescription(), s.getImage() != null ? s.getImage().getStoredFilename() : null)).toList())
                .variants(variants)
                .logs(logs)
                .build();
    }
}