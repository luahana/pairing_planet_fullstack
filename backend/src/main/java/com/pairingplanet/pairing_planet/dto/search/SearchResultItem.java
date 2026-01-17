package com.pairingplanet.pairing_planet.dto.search;

import com.pairingplanet.pairing_planet.dto.log_post.LogPostSummaryDto;
import com.pairingplanet.pairing_planet.dto.recipe.RecipeSummaryDto;

/**
 * Polymorphic search result item that can represent a recipe, log, or hashtag.
 *
 * @param type The type of content: "RECIPE", "LOG", or "HASHTAG"
 * @param relevanceScore Combined relevance score (0.0 - 1.0)
 * @param data The actual content data (RecipeSummaryDto, LogPostSummaryDto, or HashtagSearchDto)
 */
public record SearchResultItem(
    String type,
    Double relevanceScore,
    Object data
) {
    public static final String TYPE_RECIPE = "RECIPE";
    public static final String TYPE_LOG = "LOG";
    public static final String TYPE_HASHTAG = "HASHTAG";

    public static SearchResultItem recipe(RecipeSummaryDto recipe, double score) {
        return new SearchResultItem(TYPE_RECIPE, score, recipe);
    }

    public static SearchResultItem log(LogPostSummaryDto log, double score) {
        return new SearchResultItem(TYPE_LOG, score, log);
    }

    public static SearchResultItem hashtag(HashtagSearchDto hashtag, double score) {
        return new SearchResultItem(TYPE_HASHTAG, score, hashtag);
    }
}
