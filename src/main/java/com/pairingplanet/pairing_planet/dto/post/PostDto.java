package com.pairingplanet.pairing_planet.dto.post;

import com.pairingplanet.pairing_planet.domain.entity.post.DailyPost;
import com.pairingplanet.pairing_planet.domain.entity.post.Post;
import com.pairingplanet.pairing_planet.domain.entity.post.RecipePost;
import com.pairingplanet.pairing_planet.domain.entity.post.ReviewPost;
import lombok.Builder;
import java.time.Instant;

@Builder
public record PostDto(
        Long id,
        String type, // [추가] DAILY, REVIEW, RECIPE
        String content,
        String locale,

        Double popularityScore,
        Double controversyScore,
        Integer commentCount,
        Integer savedCount,

        Instant createdAt,
        String categoryTag
) {
    public static PostDto from(Post post, String tag) {
        // 타입 결정
        String dtype = "DAILY";
        if (post instanceof ReviewPost) dtype = "REVIEW";
        else if (post instanceof RecipePost) dtype = "RECIPE";

        return PostDto.builder()
                .id(post.getId())
                .type(dtype) // [추가] 타입 정보 주입
                .content(post.getContent())
                .locale(post.getLocale())
                .popularityScore(post.getPopularityScore())
                .controversyScore(post.getControversyScore())
                .commentCount(post.getCommentCount())
                .savedCount(post.getSavedCount())
                .createdAt(post.getCreatedAt())
                .categoryTag(tag)
                .build();
    }
}