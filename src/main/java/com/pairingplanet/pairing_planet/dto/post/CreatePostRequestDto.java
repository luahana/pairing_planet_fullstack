package com.pairingplanet.pairing_planet.dto.post;

import com.pairingplanet.pairing_planet.dto.food.FoodRequestDto;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public record CreatePostRequestDto(
        // --- [Common] 공통 필드 ---
        @NotNull(message = "Food1 is required")
        FoodRequestDto food1,

        FoodRequestDto food2,

        UUID whenContextId,
        UUID dietaryContextId,

        @NotEmpty(message = "At least one image is required")
        @Size(max = 3, message = "Max 3 images allowed")
        List<String> imageUrls,

        String content,
        Boolean isPrivate,
        Boolean commentsEnabled, // 댓글 허용 여부 추가

        // --- [Review] 리뷰 전용 ---
        String reviewTitle,      // 리뷰 제목
        Boolean verdictEnabled,  // 판결 기능 여부
        Integer pickyCount,      // (선택) 초기값

        // --- [Recipe] 레시피 전용 ---
        String recipeTitle,             // 레시피 제목
        String ingredients,             // 텍스트 재료 목록
        Integer cookingTime,            // 조리 시간(분)
        Integer difficulty,             // 난이도 (1:쉬움 ~ 3:어려움)
        Map<String, Object> recipeData  // 상세 단계 JSON (Step, Timer 등)
) {}