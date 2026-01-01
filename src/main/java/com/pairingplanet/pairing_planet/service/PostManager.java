package com.pairingplanet.pairing_planet.service;

import com.pairingplanet.pairing_planet.domain.entity.context.ContextTag;
import com.pairingplanet.pairing_planet.domain.entity.food.FoodMaster;
import com.pairingplanet.pairing_planet.domain.entity.food.UserSuggestedFood;
import com.pairingplanet.pairing_planet.domain.entity.hashtag.Hashtag;
import com.pairingplanet.pairing_planet.domain.entity.image.Image;
import com.pairingplanet.pairing_planet.domain.entity.pairing.PairingMap;
import com.pairingplanet.pairing_planet.domain.entity.post.Post;
import com.pairingplanet.pairing_planet.domain.entity.user.User;
import com.pairingplanet.pairing_planet.dto.food.FoodRequestDto;
import com.pairingplanet.pairing_planet.repository.context.ContextTagRepository;
import com.pairingplanet.pairing_planet.repository.food.FoodMasterRepository;
import com.pairingplanet.pairing_planet.repository.food.UserSuggestedFoodRepository;
import com.pairingplanet.pairing_planet.repository.hashtag.HashtagRepository;
import com.pairingplanet.pairing_planet.repository.image.ImageRepository;
import com.pairingplanet.pairing_planet.repository.pairing.PairingMapRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class PostManager {
    private final PairingMapRepository pairingMapRepository;
    private final FoodMasterRepository foodMasterRepository;
    private final UserSuggestedFoodRepository userSuggestedFoodRepository;
    private final ContextTagRepository contextTagRepository;
    private final HashtagRepository hashtagRepository;
    private final ImageRepository imageRepository;
    private final ImageService imageService;

    // 공통 페어링 로직
    public PairingMap processPairingLogic(User user, FoodRequestDto f1, FoodRequestDto f2, UUID whenId, UUID dietaryId) {
        FoodMaster food1 = getOrCreateFood(f1, user);
        FoodMaster food2 = (f2 != null && f2.name() != null) ? getOrCreateFood(f2, user) : null;

        ContextTag whenTag = (whenId != null)
                ? contextTagRepository.findByPublicId(whenId)
                .orElseThrow(() -> new IllegalArgumentException("Invalid When Tag"))
                : contextTagRepository.findFirstByTagName("none")
                .orElseThrow(() -> new IllegalArgumentException("Default 'NONE' tag not found"));

        ContextTag dietaryTag = (dietaryId != null)
                ? contextTagRepository.findByPublicId(dietaryId)
                .orElseThrow(() -> new IllegalArgumentException("Invalid Dietary Tag"))
                : contextTagRepository.findFirstByTagName("none")
                .orElseThrow(() -> new IllegalArgumentException("Default 'NONE' tag not found"));

        return getOrCreatePairing(food1, food2, whenTag, dietaryTag);
    }

    // 음식 생성/조회 로직
    public FoodMaster getOrCreateFood(FoodRequestDto foodReq, User user) {
        if (foodReq.name() == null || foodReq.name().isBlank()) {
            throw new IllegalArgumentException("음식 이름은 필수입니다.");
        }

        if (foodReq.id() != null) {
            return foodMasterRepository.findByPublicId(foodReq.id())
                    .orElseThrow(() -> new IllegalArgumentException("Food not found: " + foodReq.id()));
        }

        String locale = foodReq.localeCode() != null ? foodReq.localeCode() : "en";
        Optional<FoodMaster> existingFood = foodMasterRepository.findByNameAndLocale(locale, foodReq.name());
        if (existingFood.isPresent()) return existingFood.get();

        UserSuggestedFood suggested = UserSuggestedFood.builder()
                .suggestedName(foodReq.name())
                .localeCode(locale)
                .user(user)
                .status(UserSuggestedFood.SuggestionStatus.PENDING)
                .build();
        userSuggestedFoodRepository.save(suggested);

        return foodMasterRepository.save(FoodMaster.builder()
                .name(Map.of(locale, foodReq.name()))
                .isVerified(false)
                .build());
    }

    // 페어링 맵 생성/조회
    public PairingMap getOrCreatePairing(FoodMaster f1, FoodMaster f2, ContextTag when, ContextTag dietary) {
        Long id1 = f1.getId();
        Long id2 = (f2 != null) ? f2.getId() : null;

        final FoodMaster finalF1, finalF2;
        final Long finalId1, finalId2;

        if (id2 != null && id1 > id2) {
            finalF1 = f2; finalF2 = f1; finalId1 = id2; finalId2 = id1;
        } else {
            finalF1 = f1; finalF2 = f2; finalId1 = id1; finalId2 = id2;
        }

        return pairingMapRepository.findExistingPairing(finalId1, finalId2, when.getId(), dietary.getId())
                .orElseGet(() -> pairingMapRepository.save(PairingMap.builder()
                        .food1(finalF1).food2(finalF2).whenContext(when).dietaryContext(dietary).build()));
    }

    // 해시태그 처리
    public Set<Hashtag> getOrCreateHashtags(List<String> names) {
        if (names == null || names.isEmpty()) return new LinkedHashSet<>();

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
                .collect(Collectors.toCollection(LinkedHashSet::new)); // 변경 가능한 리스트로 반환
    }

    public void handleImageActivation(Post post, List<String> imageUrls, boolean isRequired, String urlPrefix) {
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
}