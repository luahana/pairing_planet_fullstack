package com.pairingplanet.pairing_planet.dto.recipe;

import com.pairingplanet.pairing_planet.domain.entity.recipe.Recipe;
import com.pairingplanet.pairing_planet.dto.log_post.LogPostSummaryDto;
import com.pairingplanet.pairing_planet.dto.hashtag.HashtagDto;
import com.pairingplanet.pairing_planet.dto.image.ImageResponseDto;
import lombok.Builder;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Builder
public record RecipeDetailResponseDto(
        UUID publicId,
        String title,
        String description,
        String culinaryLocale,
        String foodName,
        UUID foodMasterPublicId,
        String changeCategory,
        RecipeSummaryDto rootInfo,
        RecipeSummaryDto parentInfo,
        List<IngredientDto> ingredients,
        List<StepDto> steps,
        List<ImageResponseDto> images,
        List<RecipeSummaryDto> variants,
        List<LogPostSummaryDto> logs,
        List<HashtagDto> hashtags,
        Boolean isSavedByCurrentUser,
        Map<String, Object> changeDiff,
        List<String> changeCategories,
        String changeReason,
        UUID creatorPublicId,
        Boolean hasChildren
) {
    public static RecipeDetailResponseDto from(Recipe recipe, List<RecipeSummaryDto> variants, List<LogPostSummaryDto> logs, String urlPrefix, Boolean isSavedByCurrentUser, UUID creatorPublicId, Boolean hasChildren) {
        Recipe root = recipe.getRootRecipe();
        Recipe parent = recipe.getParentRecipe();

        Map<String, String> nameMap = recipe.getFoodMaster().getName();
        String currentFoodName = nameMap.getOrDefault(recipe.getCulinaryLocale(),
                nameMap.getOrDefault("ko-KR",
                        nameMap.values().stream().findFirst().orElse("Unknown Food")));
        UUID currentFoodMasterPublicId = recipe.getFoodMaster().getPublicId();

        RecipeSummaryDto rootInfo = (root != null) ? new RecipeSummaryDto(
                root.getPublicId(),
                root.getFoodMaster().getName().getOrDefault(root.getCulinaryLocale(),
                        root.getFoodMaster().getName().getOrDefault("ko-KR",
                                root.getFoodMaster().getName().values().stream().findFirst().orElse("Unknown Food"))),
                root.getFoodMaster().getPublicId(),
                root.getTitle(),
                root.getDescription(),
                root.getCulinaryLocale(),
                null,
                null,
                0,
                0,
                null,
                null,
                null
        ) : null;

        RecipeSummaryDto parentInfo = (parent != null) ? new RecipeSummaryDto(
                parent.getPublicId(),
                parent.getFoodMaster().getName().getOrDefault(parent.getCulinaryLocale(), "Unknown Food"),
                parent.getFoodMaster().getPublicId(),
                parent.getTitle(),
                parent.getDescription(),
                parent.getCulinaryLocale(),
                null,
                null,
                0,
                0,
                null,
                null,
                null
        ) : null;

        List<ImageResponseDto> imageResponses = recipe.getImages().stream()
                .map(img -> new ImageResponseDto(
                        img.getPublicId(),
                        urlPrefix + "/" + img.getStoredFilename()
                ))
                .toList();

        List<HashtagDto> hashtagDtos = recipe.getHashtags().stream()
                .map(HashtagDto::from)
                .toList();

        return RecipeDetailResponseDto.builder()
                .publicId(recipe.getPublicId())
                .title(recipe.getTitle())
                .description(recipe.getDescription())
                .culinaryLocale(recipe.getCulinaryLocale())
                .foodName(currentFoodName)
                .foodMasterPublicId(currentFoodMasterPublicId)
                .changeCategory(recipe.getChangeCategory())
                .rootInfo(rootInfo)
                .parentInfo(parentInfo)
                .ingredients(recipe.getIngredients().stream()
                        .map(i -> new IngredientDto(i.getName(), i.getAmount(), i.getType()))
                        .toList())
                .steps(recipe.getSteps().stream()
                        .map(s -> {
                            UUID imgId = (s.getImage() != null) ? s.getImage().getPublicId() : null;
                            String imgUrl = (s.getImage() != null) ? urlPrefix + "/" + s.getImage().getStoredFilename() : null;
                            return new StepDto(s.getStepNumber(), s.getDescription(), imgId, imgUrl);
                        })
                        .toList())
                .images(imageResponses)
                .variants(variants)
                .logs(logs)
                .hashtags(hashtagDtos)
                .isSavedByCurrentUser(isSavedByCurrentUser)
                .changeDiff(recipe.getChangeDiff())
                .changeCategories(recipe.getChangeCategories())
                .changeReason(recipe.getChangeReason())
                .creatorPublicId(creatorPublicId)
                .hasChildren(hasChildren)
                .build();
    }
}
