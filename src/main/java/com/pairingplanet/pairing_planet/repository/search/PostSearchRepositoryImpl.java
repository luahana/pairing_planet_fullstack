package com.pairingplanet.pairing_planet.repository.search;

import com.pairingplanet.pairing_planet.domain.entity.food.FoodMaster;
import com.pairingplanet.pairing_planet.domain.entity.food.QFoodMaster;
import com.pairingplanet.pairing_planet.domain.entity.context.QContextTag;
import com.pairingplanet.pairing_planet.dto.search.PairingSearchRequestDto;
import com.pairingplanet.pairing_planet.dto.search.PostSearchResultDto;
import com.pairingplanet.pairing_planet.dto.search.SearchCursorDto;
import com.pairingplanet.pairing_planet.repository.food.FoodMasterRepository;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.Tuple;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static com.pairingplanet.pairing_planet.domain.entity.post.QPost.post;
import static com.pairingplanet.pairing_planet.domain.entity.pairing.QPairingMap.pairingMap;
import static com.pairingplanet.pairing_planet.domain.entity.user.QUser.user;

@Repository
@RequiredArgsConstructor
public class PostSearchRepositoryImpl implements PostSearchRepositoryCustom {
    private final JPAQueryFactory queryFactory;
    private final FoodMasterRepository foodMasterRepository;

    @Value("${file.upload.url-prefix}")
    private String urlPrefix;

    private static final QFoodMaster f1 = new QFoodMaster("f1");
    private static final QFoodMaster f2 = new QFoodMaster("f2");
    private static final QContextTag ctxWhen = new QContextTag("ctxWhen");
    private static final QContextTag ctxDiet = new QContextTag("ctxDiet");

    @Override
    public List<PostSearchResultDto> searchPosts(PairingSearchRequestDto request, SearchCursorDto cursor, int limit) {
        return executeSearch(request, cursor, limit, false);
    }

    @Override
    public List<PostSearchResultDto> searchPostsFallback(PairingSearchRequestDto request, SearchCursorDto cursor, int limit) {
        return executeSearch(request, cursor, limit, true);
    }

    private List<PostSearchResultDto> executeSearch(PairingSearchRequestDto request, SearchCursorDto cursor, int limit, boolean ignoreWhenTag) {
        return queryFactory
                .select(post, user, f1, f2, ctxWhen, ctxDiet)
                .from(post)
                .join(post.pairing, pairingMap)
                .join(post.creator, user)
                .join(pairingMap.food1, f1)
                .leftJoin(pairingMap.food2, f2)
                .leftJoin(pairingMap.whenContext, ctxWhen)
                .leftJoin(pairingMap.dietaryContext, ctxDiet)
                .where(
                        post.isDeleted.isFalse(),
                        post.isPrivate.isFalse(),
                        buildFilterConditions(request, ignoreWhenTag),
                        buildCursorCondition(cursor)
                )
                .orderBy(post.popularityScore.desc(), post.createdAt.desc(), post.id.desc())
                .limit(limit)
                .fetch()
                .stream()
                .map(tuple -> convertToDto(tuple, request.locale(), ignoreWhenTag))
                .toList();
    }

    private BooleanBuilder buildFilterConditions(PairingSearchRequestDto request, boolean ignoreWhenTag) {
        BooleanBuilder builder = new BooleanBuilder();
        if (request.foodIds() != null && !request.foodIds().isEmpty()) {
            List<Long> internalFoodIds = foodMasterRepository.findByPublicIdIn(request.foodIds())
                    .stream()
                    .map(FoodMaster::getId)
                    .toList();
            if (internalFoodIds.size() == 1) {
                Long id = internalFoodIds.get(0);
                builder.and(pairingMap.food1.id.eq(id).or(pairingMap.food2.id.eq(id)));
            } else if (internalFoodIds.size() >= 2) {
                Long id1 = internalFoodIds.get(0);
                Long id2 = internalFoodIds.get(1);
                builder.and(
                        (pairingMap.food1.id.eq(id1).and(pairingMap.food2.id.eq(id2)))
                                .or(pairingMap.food1.id.eq(id2).and(pairingMap.food2.id.eq(id1)))
                );
            }
        }
        if (request.dietaryContextId() != null) builder.and(pairingMap.dietaryContext.id.eq(request.dietaryContextId()));
        if (!ignoreWhenTag && request.whenContextId() != null) builder.and(pairingMap.whenContext.id.eq(request.whenContextId()));
        return builder;
    }

    private BooleanBuilder buildCursorCondition(SearchCursorDto cursor) {
        if (cursor == null || cursor.lastScore() == null) return new BooleanBuilder();
        Instant safeCreatedAt = cursor.lastCreatedAt();
        if (safeCreatedAt == null || safeCreatedAt.isBefore(Instant.parse("1970-01-01T00:00:00Z"))) {
            safeCreatedAt = Instant.parse("1970-01-01T00:00:00Z");
        }
        BooleanBuilder builder = new BooleanBuilder();
        builder.and(
                post.popularityScore.lt(cursor.lastScore())
                        .or(post.popularityScore.eq(cursor.lastScore()).and(post.createdAt.lt(safeCreatedAt)))
                        .or(post.popularityScore.eq(cursor.lastScore()).and(post.createdAt.eq(safeCreatedAt)).and(post.id.lt(cursor.lastId())))
        );
        return builder;
    }

    private PostSearchResultDto convertToDto(Tuple tuple, String locale, boolean isFallback) {
        var p = tuple.get(post);
        var u = tuple.get(user);
        var food1 = tuple.get(f1);
        var food2 = tuple.get(f2);
        var when = tuple.get(ctxWhen);
        var diet = tuple.get(ctxDiet);

        return new PostSearchResultDto(
                p.getId(), p.getPublicId(), p.getContent(),
                p.getImages() != null ? p.getImages().stream().map(img -> urlPrefix + "/" + img.getStoredFilename()).collect(Collectors.toList()) : Collections.emptyList(),
                p.getCreatedAt(),
                u.getUsername(), u.getPublicId(),
                getLocalizedName(food1, locale), food1.getPublicId(),
                getLocalizedName(food2, locale), food2 != null ? food2.getPublicId() : null,
                // [수정] getDisplayName() -> getDisplayNameByLocale(locale)
                when != null ? when.getDisplayNameByLocale(locale) : null,
                diet != null ? diet.getDisplayNameByLocale(locale) : null,
                p.getGeniusCount(), p.getDaringCount(), p.getPickyCount(), p.getCommentCount(), p.getSavedCount(), p.getPopularityScore(),
                isFallback
        );
    }

    private String getLocalizedName(FoodMaster food, String locale) {
        if (food == null) return "Unknown Food";
        // [수정] FoodMaster 엔티티의 다국어 지원 메서드 호출
        return food.getNameByLocale(locale);
    }
}