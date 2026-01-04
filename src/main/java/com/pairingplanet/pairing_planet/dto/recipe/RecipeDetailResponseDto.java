package com.pairingplanet.pairing_planet.dto.recipe;

import com.pairingplanet.pairing_planet.domain.entity.recipe.Recipe;
import com.pairingplanet.pairing_planet.dto.image.ImageResponseDto; // [추가]
import com.pairingplanet.pairing_planet.dto.log_post.LogPostSummaryDto;
import lombok.Builder;

import java.util.List;
import java.util.UUID;

@Builder
public record RecipeDetailResponseDto(
        UUID publicId,
        String title,
        String description,
        String culinaryLocale,
        String changeCategory,
        RecipeSummaryDto rootInfo,
        RecipeSummaryDto parentInfo,
        List<IngredientDto> ingredients,
        List<StepDto> steps,
        List<ImageResponseDto> images, // [수정] List<String> -> List<ImageResponseDto>
        List<RecipeSummaryDto> variants,
        List<LogPostSummaryDto> logs
) {
    /**
     * Entity -> DTO 변환 (urlPrefix를 매개변수로 받음)
     */
    public static RecipeDetailResponseDto from(Recipe recipe, List<RecipeSummaryDto> variants, List<LogPostSummaryDto> logs, String urlPrefix) {
        Recipe root = recipe.getRootRecipe();
        Recipe parent = recipe.getParentRecipe();

        // 1. 대표 이미지 리스트 변환 (UUID + URL)
        List<ImageResponseDto> imageResponses = recipe.getImages().stream()
                .map(img -> new ImageResponseDto(img.getPublicId(), urlPrefix + "/" + img.getStoredFilename()))
                .toList();

        return RecipeDetailResponseDto.builder()
                .publicId(recipe.getPublicId())
                .title(recipe.getTitle())
                .description(recipe.getDescription())
                .culinaryLocale(recipe.getCulinaryLocale())
                .changeCategory(recipe.getChangeCategory())
                .rootInfo(root != null ? new RecipeSummaryDto(root.getPublicId(), root.getTitle(), root.getCulinaryLocale(), null, null) : null)
                .parentInfo(parent != null ? new RecipeSummaryDto(parent.getPublicId(), parent.getTitle(), null, null, null) : null)
                .ingredients(recipe.getIngredients().stream().map(i -> new IngredientDto(i.getName(), i.getAmount(), i.getType())).toList())
                .steps(recipe.getSteps().stream()
                        .map(s -> {
                            UUID imgId = (s.getImage() != null) ? s.getImage().getPublicId() : null;
                            String imgUrl = (s.getImage() != null) ? urlPrefix + "/" + s.getImage().getStoredFilename() : null;
                            return new StepDto(s.getStepNumber(), s.getDescription(), imgId, imgUrl);
                        })
                        .toList())
                .images(imageResponses) // [적용]
                .variants(variants)
                .logs(logs)
                .build();
    }
}