package com.cookstemma.cookstemma.dto.log_post;

import com.cookstemma.cookstemma.dto.hashtag.HashtagDto;
import com.cookstemma.cookstemma.dto.image.ImageResponseDto;
import com.cookstemma.cookstemma.dto.recipe.RecipeSummaryDto;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public record LogPostDetailResponseDto(
        UUID publicId,
        String title,
        String content,
        Integer rating,  // 1-5 star rating
        List<ImageResponseDto> images,
        RecipeSummaryDto linkedRecipe,
        Instant createdAt,
        List<HashtagDto> hashtags,
        Boolean isSavedByCurrentUser,  // null if not logged in
        UUID creatorPublicId,  // for ownership check (UUID for frontend comparison)
        String userName,     // Creator's username for display
        // Translations (async populated by OpenAI GPT)
        Map<String, String> titleTranslations,    // {"en": "...", "ja": "...", ...}
        Map<String, String> contentTranslations   // {"en": "...", "ja": "...", ...}
) {}