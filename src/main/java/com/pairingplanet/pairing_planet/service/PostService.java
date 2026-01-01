package com.pairingplanet.pairing_planet.service;

import com.pairingplanet.pairing_planet.domain.entity.context.ContextTag;
import com.pairingplanet.pairing_planet.domain.entity.food.FoodMaster;
import com.pairingplanet.pairing_planet.domain.entity.food.UserSuggestedFood;
import com.pairingplanet.pairing_planet.domain.entity.hashtag.Hashtag;
import com.pairingplanet.pairing_planet.domain.entity.image.Image;
import com.pairingplanet.pairing_planet.domain.entity.pairing.PairingMap;
import com.pairingplanet.pairing_planet.domain.entity.post.*;
import com.pairingplanet.pairing_planet.domain.entity.post.daily_log.DailyPost;
import com.pairingplanet.pairing_planet.domain.entity.post.discussion.DiscussionPost;
import com.pairingplanet.pairing_planet.domain.entity.user.User;
import com.pairingplanet.pairing_planet.dto.food.FoodRequestDto;
import com.pairingplanet.pairing_planet.dto.post.CreatePostRequestDto;
import com.pairingplanet.pairing_planet.dto.post.PostResponseDto;
import com.pairingplanet.pairing_planet.repository.context.ContextTagRepository;
import com.pairingplanet.pairing_planet.repository.food.FoodMasterRepository;
import com.pairingplanet.pairing_planet.repository.food.UserSuggestedFoodRepository;
import com.pairingplanet.pairing_planet.repository.hashtag.HashtagRepository;
import com.pairingplanet.pairing_planet.repository.image.ImageRepository;
import com.pairingplanet.pairing_planet.repository.pairing.PairingMapRepository;
import com.pairingplanet.pairing_planet.repository.post.PostRepository;
import com.pairingplanet.pairing_planet.repository.user.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


import java.time.Duration;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class PostService {
    private final RedisTemplate<String, String> redisTemplate;
    private final PostRepository postRepository;
    private final PairingMapRepository pairingMapRepository;
    private final FoodMasterRepository foodMasterRepository;
    private final UserSuggestedFoodRepository userSuggestedFoodRepository;
    private final ContextTagRepository contextTagRepository;
    private final UserRepository userRepository;
    private final ImageRepository  imageRepository;
    private final ImageService imageService;
    private final HashtagRepository hashtagRepository;

    @Value("${file.upload.url-prefix}")
    private String urlPrefix;

    // ==========================================
    // 1. Create Methods
    // ==========================================

    @Transactional
    public PostResponseDto createDailyPost(UUID userId, CreatePostRequestDto request, String idempotencyKey) {
        if (idempotencyKey != null) {
            String redisKey = "idempotency:post:" + idempotencyKey;
            String existingPostId = redisTemplate.opsForValue().get(redisKey);
            if (existingPostId != null) {
                return getPostResponseByPublicId(UUID.fromString(existingPostId));
            }
        }

        User user = getUser(userId);
        PairingMap pairing = processPairingLogic(user, request);

        boolean isPrivate = Boolean.TRUE.equals(request.isPrivate());
        boolean isCommentsEnabled = !isPrivate && (request.commentsEnabled() == null || request.commentsEnabled());

        DailyPost post = DailyPost.builder()
                .creator(user)
                .pairing(pairing)
                .locale(user.getLocale() != null ? user.getLocale() : "en")
                .content(request.content())
                .isPrivate(isPrivate)
                .commentsEnabled(isCommentsEnabled)
                .hashtags(getOrCreateHashtags(request.hashtags())) // [추가] 유저 직접 입력 태그 연결
                .build();

        Post savedPost = postRepository.save(post);
        handleImageActivation(savedPost, request.imageUrls(), true);

        if (idempotencyKey != null) {
            redisTemplate.opsForValue().set("idempotency:post:" + idempotencyKey, savedPost.getPublicId().toString(), Duration.ofHours(1));
        }

        return PostResponseDto.from(savedPost, urlPrefix);
    }

    @Transactional
    public PostResponseDto createDiscussionPost(UUID userId, CreatePostRequestDto request, String idempotencyKey) {
        if (idempotencyKey != null) {
            String redisKey = "idempotency:post:" + idempotencyKey;
            String existingPostId = redisTemplate.opsForValue().get(redisKey);
            if (existingPostId != null) {
                return getPostResponseByPublicId(UUID.fromString(existingPostId));
            }
        }

        User user = getUser(userId);
        PairingMap pairing = processPairingLogic(user, request);

        boolean isPrivate = Boolean.TRUE.equals(request.isPrivate());
        boolean isVerdictEnabled = !isPrivate && (request.verdictEnabled() == null || request.verdictEnabled());
        boolean isCommentsEnabled = !isPrivate && isVerdictEnabled;

        DiscussionPost post = DiscussionPost.builder()
                .creator(user)
                .pairing(pairing)
                .locale(user.getLocale() != null ? user.getLocale() : "en")
                .content(request.content())
                .isPrivate(isPrivate)
                .commentsEnabled(isCommentsEnabled)
                .title(request.discussionTitle())
                .verdictEnabled(isVerdictEnabled)
                .hashtags(getOrCreateHashtags(request.hashtags())) // [추가] 유저 직접 입력 태그 연결
                .build();

        Post savedPost = postRepository.save(post);
        handleImageActivation(savedPost, request.imageUrls(), false);

        if (idempotencyKey != null) {
            redisTemplate.opsForValue().set("idempotency:post:" + idempotencyKey, savedPost.getPublicId().toString(), Duration.ofHours(1));
        }

        return PostResponseDto.from(savedPost, urlPrefix);
    }
    // ==========================================
    // 2. Update Method
    // ==========================================

    @Transactional
    public PostResponseDto updatePost(UUID userId, UUID postId, CreatePostRequestDto request) {
        Post post = postRepository.findByPublicId(postId)
                .orElseThrow(() -> new IllegalArgumentException("Post not found: " + postId));

        if (!post.getCreator().getPublicId().equals(userId)) {
            throw new IllegalArgumentException("Unauthorized: You are not the creator of this post.");
        }

        // 공통 필드 업데이트
        if (request.content() != null) post.updateContent(request.content());
        if (request.isPrivate() != null) post.setPrivate(request.isPrivate());

        // [추가] 해시태그 업데이트 로직
        if (request.hashtags() != null) {
            post.setHashtags(getOrCreateHashtags(request.hashtags()));
        }

        // 타입별 필드 업데이트
        if (post instanceof DiscussionPost discussion) {
            if (request.discussionTitle() != null) discussion.setTitle(request.discussionTitle());
            if (request.verdictEnabled() != null) discussion.setVerdictEnabled(request.verdictEnabled());
        }

        return PostResponseDto.from(post, urlPrefix);
    }

    // ==========================================
    // 3. Delete Method
    // ==========================================

    @Transactional
    public void deletePost(UUID userId, UUID postId) {
        Post post = postRepository.findByPublicId(postId)
                .orElseThrow(() -> new IllegalArgumentException("Post not found: " + postId));

        if (!post.getCreator().getPublicId().equals(userId)) {
            throw new IllegalArgumentException("Unauthorized");
        }

        post.softDelete();
    }

    // ==========================================
    // 4. Private Helper Methods
    // ==========================================

    private User getUser(UUID userId) {
        return userRepository.findByPublicId(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
    }

    private PairingMap processPairingLogic(User user, CreatePostRequestDto request) {
        FoodMaster food1 = getOrCreateFood(request.food1(), user);
        FoodMaster food2 = (request.food2() != null && request.food2().name() != null)
                ? getOrCreateFood(request.food2(), user)
                : null;

        ContextTag whenTag = (request.whenContextId() != null)
                ? contextTagRepository.findByPublicId(request.whenContextId())
                .orElseThrow(() -> new IllegalArgumentException("Invalid When Tag"))
                : contextTagRepository.findFirstByTagName("none")
                .orElseThrow(() -> new IllegalArgumentException("Default 'NONE' tag not found"));

        ContextTag dietaryTag = (request.dietaryContextId() != null)
                ? contextTagRepository.findByPublicId(request.dietaryContextId())
                .orElseThrow(() -> new IllegalArgumentException("Invalid Dietary Tag"))
                : contextTagRepository.findFirstByTagName("none")
                .orElseThrow(() -> new IllegalArgumentException("Default 'NONE' tag not found"));

        return getOrCreatePairing(food1, food2, whenTag, dietaryTag);
    }

    private FoodMaster getOrCreateFood(FoodRequestDto foodReq, User user) {
        if (foodReq.id() != null) {
            return foodMasterRepository.findByPublicId(foodReq.id())
                    .orElseThrow(() -> new IllegalArgumentException("Food not found: " + foodReq.id()));
        }

        String locale = foodReq.localeCode() != null ? foodReq.localeCode() : "en";

        Optional<FoodMaster> existingFood = foodMasterRepository.findByNameAndLocale(locale, foodReq.name());
        if (existingFood.isPresent()) {
            return existingFood.get(); // 이미 있다면 해당 엔티티 반환
        }

        UserSuggestedFood suggested = UserSuggestedFood.builder()
                .suggestedName(foodReq.name())
                .localeCode(locale)
                .user(user)
                .status(UserSuggestedFood.SuggestionStatus.PENDING)
                .build();
        userSuggestedFoodRepository.save(suggested);

        FoodMaster tempFood = FoodMaster.builder()
                .name(Map.of(locale, foodReq.name()))
                .isVerified(false)
                .build();

        return foodMasterRepository.save(tempFood);
    }

    private PairingMap getOrCreatePairing(FoodMaster f1, FoodMaster f2, ContextTag when, ContextTag dietary) {
        Long id1 = f1.getId();
        Long id2 = (f2 != null) ? f2.getId() : null;

        // 1. 실제 저장에 사용할 변수를 미리 선언 (람다 밖에서 결정)
        final FoodMaster finalF1;
        final FoodMaster finalF2;
        final Long finalId1;
        final Long finalId2;

        // 2. 조건에 따라 단 한 번만 값을 할당 (Effectively Final 상태 유지)
        if (id2 != null && id1 > id2) {
            finalF1 = f2;
            finalF2 = f1;
            finalId1 = id2;
            finalId2 = id1;
        } else {
            finalF1 = f1;
            finalF2 = f2;
            finalId1 = id1;
            finalId2 = id2;
        }

        // 3. 이제 람다 내부에서 final 변수들을 안전하게 사용 가능
        return pairingMapRepository.findExistingPairing(finalId1, finalId2, when.getId(), dietary.getId())
                .orElseGet(() -> pairingMapRepository.save(
                        PairingMap.builder()
                                .food1(finalF1)
                                .food2(finalF2)
                                .whenContext(when)
                                .dietaryContext(dietary)
                                .build()
                ));
    }

    private void handleImageActivation(Post post, List<String> imageUrls, boolean isRequired) {
        if (imageUrls == null || imageUrls.isEmpty()) {
            if (isRequired) {
                throw new IllegalArgumentException("Image is required for posting.");
            }
            return;
        }

        imageService.activateImages(imageUrls);

        List<Image> images = imageRepository.findByStoredFilenameIn(
                imageUrls.stream()
                        .map(url -> url.replace(urlPrefix + "/", ""))
                        .toList()
        );

        if (images.isEmpty()) {
            throw new IllegalArgumentException("Invalid image URLs provided.");
        }

        for (Image image : images) {
            image.setPost(post);
        }
    }

    private PostResponseDto getPostResponseByPublicId(UUID postId) {
        Post post = postRepository.findByPublicId(postId)
                .orElseThrow(() -> new IllegalArgumentException("Post not found"));
        return PostResponseDto.from(post, urlPrefix);
    }

    /**
     * 유저가 입력한 문자열 태그를 Hashtag 엔티티 리스트로 변환 (중복 처리 포함)
     */
    private List<Hashtag> getOrCreateHashtags(List<String> names) {
        if (names == null || names.isEmpty()) return new ArrayList<>();

        // 1. 공백 제거 및 중복 입력 방지
        List<String> cleanNames = names.stream()
                .map(String::trim)
                .filter(name -> !name.isEmpty())
                .distinct()
                .toList();

        // 2. DB 조회 또는 생성하여 반환
        return cleanNames.stream()
                .map(name -> hashtagRepository.findByName(name)
                        .orElseGet(() -> hashtagRepository.save(Hashtag.builder().name(name).build())))
                .collect(Collectors.toCollection(ArrayList::new)); // 변경 가능한 리스트로 반환
    }
}