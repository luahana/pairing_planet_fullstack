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
    private final UserRepository userRepository;

    private final PostManager postManager;

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
        PairingMap pairing = postManager.processPairingLogic(user, request.food1(), request.food2(),
                request.whenContextId(), request.dietaryContextId());

        boolean isPrivate = Boolean.TRUE.equals(request.isPrivate());
        boolean isCommentsEnabled = !isPrivate && (request.commentsEnabled() == null || request.commentsEnabled());

        DailyPost post = DailyPost.builder()
                .creator(user)
                .pairing(pairing)
                .locale(user.getLocale() != null ? user.getLocale() : "en")
                .content(request.content())
                .isPrivate(isPrivate)
                .commentsEnabled(isCommentsEnabled)
                .hashtags(postManager.getOrCreateHashtags(request.hashtags())) // [추가] 유저 직접 입력 태그 연결
                .build();

        Post savedPost = postRepository.save(post);
        postManager.handleImageActivation(savedPost, request.imageUrls(), true, urlPrefix);

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
        PairingMap pairing = postManager.processPairingLogic(user, request.food1(), request.food2(),
                request.whenContextId(), request.dietaryContextId());

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
                .hashtags(postManager.getOrCreateHashtags(request.hashtags())) // [추가] 유저 직접 입력 태그 연결
                .build();

        Post savedPost = postRepository.save(post);
        postManager.handleImageActivation(savedPost, request.imageUrls(), false, urlPrefix);

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
            post.setHashtags(postManager.getOrCreateHashtags(request.hashtags()));
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

    private PostResponseDto getPostResponseByPublicId(UUID postId) {
        Post post = postRepository.findByPublicId(postId)
                .orElseThrow(() -> new IllegalArgumentException("Post not found"));
        return PostResponseDto.from(post, urlPrefix);
    }
}