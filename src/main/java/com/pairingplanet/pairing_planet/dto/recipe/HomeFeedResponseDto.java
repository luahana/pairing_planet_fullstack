package com.pairingplanet.pairing_planet.dto.recipe;

import lombok.Builder;

import java.util.List;

@Builder
public record HomeFeedResponseDto(
        List<RecipeSummaryDto> recentRecipes,    // 최근 생성된 레시피
        List<TrendingTreeDto> trendingTrees      // "이 레시피, 이렇게 바뀌고 있어요" 섹션용
) {}