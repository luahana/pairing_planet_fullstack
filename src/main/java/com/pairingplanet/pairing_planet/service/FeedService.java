package com.pairingplanet.pairing_planet.service;

import com.pairingplanet.pairing_planet.domain.entity.post.*;
import com.pairingplanet.pairing_planet.domain.entity.user.User;
import com.pairingplanet.pairing_planet.dto.post.CursorResponse; // [변경]
import com.pairingplanet.pairing_planet.dto.post.PostResponseDto; // [변경]
import com.pairingplanet.pairing_planet.repository.post.PostRepository;
import com.pairingplanet.pairing_planet.repository.user.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class FeedService {

    private final PostRepository postRepository;
    private final UserRepository userRepository;
    private final RedisTemplate<String, Object> redisTemplate;

    private static final String KEY_DAILY = "feed:daily";
    private static final String KEY_DISCUSSION = "feed:discussion";
    private static final String KEY_RECIPE = "feed:recipe";

    private static final int PAGE_SIZE = 10;
    private static final long HISTORY_TTL_DAYS = 1;

    @Value("${file.upload.url-prefix}") // 하드코딩 제거
    private String urlPrefix;

    /**
     * 메인 피드 진입점: CursorResponse<PostResponseDto>로 반환 타입 통일
     */
    public CursorResponse<PostResponseDto> getMixedFeed(UUID userPublicId, int offset) {
        User user = (userPublicId != null) ?
                userRepository.findByPublicId(userPublicId).orElse(null) : null;
        Long preferredDietaryId = (user != null) ? user.getPreferredDietaryId() : null;

        try {
            if (preferredDietaryId != null) {
                return getPersonalizedFeedFromDb(preferredDietaryId, offset);
            }
            return getFeedFromRedis(userPublicId, offset);
        } catch (Exception e) {
            log.error("Feed error, switching to Global DB Fallback: {}", e.getMessage());
            return getFeedFallback(offset);
        }
    }

    private CursorResponse<PostResponseDto> getPersonalizedFeedFromDb(Long dietaryId, int offset) {
        List<PostResponseDto> combinedPosts = new ArrayList<>();

        combinedPosts.addAll(fetchFromDbWithFilter(DailyPost.class, dietaryId, 4, offset));
        combinedPosts.addAll(fetchFromDbWithFilter(DiscussionPost.class, dietaryId, 4, offset));
        combinedPosts.addAll(fetchFromDbWithFilter(RecipePost.class, dietaryId, 2, offset));

        Collections.shuffle(combinedPosts);

        return new CursorResponse<>(combinedPosts, String.valueOf(offset + 1), combinedPosts.size() >= PAGE_SIZE);
    }

    private CursorResponse<PostResponseDto> getFeedFromRedis(UUID userId, int offset) {
        Long internalUserId = (userId != null) ?
                userRepository.findByPublicId(userId).map(User::getId).orElse(null) : null;
        String historyKey = (internalUserId != null) ? "user:" + internalUserId + ":seen" : "user:anon:seen";

        List<PostResponseDto> finalPosts = new ArrayList<>();

        finalPosts.addAll(fetchAndFilterFromRedis(KEY_DAILY, 4, offset, historyKey));
        finalPosts.addAll(fetchAndFilterFromRedis(KEY_DISCUSSION, 4, offset, historyKey));
        finalPosts.addAll(fetchAndFilterFromRedis(KEY_RECIPE, 2, offset, historyKey));

        Collections.shuffle(finalPosts);

        return new CursorResponse<>(finalPosts, String.valueOf(offset + 1), finalPosts.size() >= PAGE_SIZE);
    }

    private CursorResponse<PostResponseDto> getFeedFallback(int offset) {
        List<PostResponseDto> combinedPosts = new ArrayList<>();

        combinedPosts.addAll(fetchFromDbWithFilter(DailyPost.class, null, 4, offset));
        combinedPosts.addAll(fetchFromDbWithFilter(DiscussionPost.class, null, 4, offset));
        combinedPosts.addAll(fetchFromDbWithFilter(RecipePost.class, null, 2, offset));

        return new CursorResponse<>(combinedPosts, String.valueOf(offset + 1), combinedPosts.size() >= PAGE_SIZE);
    }

    // --- Helper Methods ---

    private List<PostResponseDto> fetchAndFilterFromRedis(String key, int count, int offset, String historyKey) {
        int start = offset * count;
        List<Object> rawIds = redisTemplate.opsForList().range(key, start, start + (count * 2));
        if (rawIds == null || rawIds.isEmpty()) return new ArrayList<>();

        List<Long> filteredIds = new ArrayList<>();
        for (Object obj : rawIds) {
            String idStr = obj.toString();
            if (Boolean.FALSE.equals(redisTemplate.opsForSet().isMember(historyKey, idStr))) {
                filteredIds.add(Long.valueOf(idStr));
                if (filteredIds.size() >= count) break;
            }
        }

        if (filteredIds.isEmpty()) return new ArrayList<>();

        redisTemplate.opsForSet().add(historyKey, filteredIds.stream().map(String::valueOf).toArray(String[]::new));
        redisTemplate.expire(historyKey, HISTORY_TTL_DAYS, TimeUnit.DAYS);

        return postRepository.findAllWithDetailsByIdIn(filteredIds).stream()
                .map(p -> PostResponseDto.from(p, urlPrefix)) // [수정] 인자 2개 버전 사용
                .toList();
    }

    private List<PostResponseDto> fetchFromDbWithFilter(Class<? extends Post> type, Long dietaryId, int limit, int offset) {
        return postRepository.findPublicPostsWithPreference(type, dietaryId, PageRequest.of(offset, limit))
                .getContent().stream()
                .map(p -> PostResponseDto.from(p, urlPrefix)) // [수정] 인자 2개 버전 사용
                .toList();
    }
}