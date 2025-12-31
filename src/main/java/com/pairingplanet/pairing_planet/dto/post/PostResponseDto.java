package com.pairingplanet.pairing_planet.dto.post;

import com.pairingplanet.pairing_planet.domain.entity.hashtag.Hashtag;
import com.pairingplanet.pairing_planet.domain.entity.pairing.PairingMap;
import com.pairingplanet.pairing_planet.domain.entity.post.DailyPost;
import com.pairingplanet.pairing_planet.domain.entity.post.DiscussionPost;
import com.pairingplanet.pairing_planet.domain.entity.post.Post;
import com.pairingplanet.pairing_planet.domain.entity.post.RecipePost;
import lombok.Builder;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Builder
public record PostResponseDto(
        UUID id,
        UUID creatorId,
        String type,
        String content,
        List<String> imageUrls,
        Instant createdAt,
        Boolean isPrivate,
        String food1Name,
        String food2Name,
        String whenContext,
        String dietaryContext,
        List<String> hashtags,
        int geniusCount,
        int daringCount,
        int pickyCount,
        int savedCount,
        int commentCount,
        String discussionTitle,
        Boolean verdictEnabled,
        String recipeTitle,
        String ingredients,
        Integer cookingTime,
        Integer difficulty,
        Map<String, Object> recipeData
) {
    public static PostResponseDto from(Post post, String urlPrefix) {
        var builder = PostResponseDto.builder()
                .id(post.getPublicId())
                .creatorId(post.getCreator() != null ? post.getCreator().getPublicId() : null)
                .content(post.getContent())
                .imageUrls(post.getImages().stream()
                        .map(img -> urlPrefix + "/" + img.getStoredFilename())
                        .collect(Collectors.toList()))
                .createdAt(post.getCreatedAt())
                .isPrivate(post.isPrivate())
                .geniusCount(post.getGeniusCount())
                .daringCount(post.getDaringCount())
                .pickyCount(post.getPickyCount())
                .savedCount(post.getSavedCount())
                .commentCount(post.getCommentCount())
                .hashtags(post.getHashtags().stream()
                        .map(Hashtag::getName)
                        .toList());

        if (post.getPairing() != null) {
            PairingMap pairing = post.getPairing();
            String postLocale = post.getLocale(); // 포스트의 로케일 정보 추출

            // [수정] FoodsMaster와 ContextTag 모두 변경된 다국어 지원 메서드 호출
            builder.food1Name(pairing.getFood1() != null ? pairing.getFood1().getNameByLocale(postLocale) : null);
            builder.food2Name(pairing.getFood2() != null ? pairing.getFood2().getNameByLocale(postLocale) : null);

            // [수정] getDisplayName() -> getDisplayNameByLocale(postLocale)
            builder.whenContext(pairing.getWhenContext() != null ?
                    pairing.getWhenContext().getDisplayNameByLocale(postLocale) : null);
            builder.dietaryContext(pairing.getDietaryContext() != null ?
                    pairing.getDietaryContext().getDisplayNameByLocale(postLocale) : null);
        }

        if (post instanceof DailyPost) {
            builder.type("DAILY");
        } else if (post instanceof DiscussionPost discussion) {
            builder.type("DISCUSSION")
                    .discussionTitle(discussion.getTitle())
                    .verdictEnabled(discussion.isVerdictEnabled());
        } else if (post instanceof RecipePost recipe) {
            builder.type("RECIPE")
                    .recipeTitle(recipe.getTitle())
                    .ingredients(recipe.getIngredients())
                    .cookingTime(recipe.getCookingTime())
                    .difficulty(recipe.getDifficulty());
        }

        return builder.build();
    }
}