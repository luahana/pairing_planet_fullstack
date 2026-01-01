package com.pairingplanet.pairing_planet.service;

import com.pairingplanet.pairing_planet.domain.entity.post.Post;
import com.pairingplanet.pairing_planet.domain.entity.post.recipe.*;
import com.pairingplanet.pairing_planet.domain.entity.user.User;
import com.pairingplanet.pairing_planet.domain.enums.IngredientType;
import com.pairingplanet.pairing_planet.dto.post.recipe.IngredientRequestDto;
import com.pairingplanet.pairing_planet.dto.post.recipe.RecipeDetailResponseDto;
import com.pairingplanet.pairing_planet.dto.post.recipe.RecipeRequestDto;
import com.pairingplanet.pairing_planet.dto.post.recipe.StepRequestDto;
import com.pairingplanet.pairing_planet.repository.post.PostRepository;
import com.pairingplanet.pairing_planet.repository.post.recipe.RecipeEditLogRepository;
import com.pairingplanet.pairing_planet.repository.post.recipe.RecipeIngredientRepository;
import com.pairingplanet.pairing_planet.repository.post.recipe.RecipeRepository;
import com.pairingplanet.pairing_planet.repository.post.recipe.RecipeStepRepository;
import com.pairingplanet.pairing_planet.repository.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class RecipeService {
    private final PostRepository postRepository;
    private final RecipeRepository recipeRepository;
    private final RecipeIngredientRepository ingredientRepository;
    private final RecipeStepRepository stepRepository;
    private final RecipeEditLogRepository editLogRepository;
    private final UserRepository userRepository;

    // 1. 레시피 생성 (신규 또는 변형)
    public RecipeDetailResponseDto saveRecipe(UUID userPublicId, RecipeRequestDto req, UUID sourcePublicId) {
        User user = userRepository.findByPublicId(userPublicId).orElseThrow();

        // Post(게시글) 엔티티 생성
        RecipePost post = new RecipePost();
        post.setCreator(user);
        post.setLocale(user.getLocale()); // 시스템 로케일 반영
        post.setContent(req.description());
        postRepository.save(post);

        Long rootId = null;
        Long parentId = null;

        // 변형 레시피인 경우 계층 구조 계산
        if (sourcePublicId != null) {
            Post sourcePost = postRepository.findByPublicId(sourcePublicId).orElseThrow();
            Recipe sourceRecipe = recipeRepository.findLatestByPostId(sourcePost.getId()).orElseThrow();

            // 변형의 변형이라도 root는 오리지널을 가리킴 (Direct Child 구조)
            rootId = (sourceRecipe.getRootRecipeId() == null) ? sourcePost.getId() : sourceRecipe.getRootRecipeId();
            parentId = sourcePost.getId();
        }

        saveVersion(post.getId(), 1, rootId, parentId, req, user.getId());
        return getRecipeDetail(post.getPublicId());
    }

    // 2. 새로운 버전 추가 (수정 시 호출)
    public void createNewVersion(UUID postPublicId, UUID userPublicId, RecipeRequestDto req) {
        Post post = postRepository.findByPublicId(postPublicId).orElseThrow();
        User editor = userRepository.findByPublicId(userPublicId).orElseThrow();

        // 최신 버전 확인 후 +1
        int latestVersion = recipeRepository.findMaxVersionByPostId(post.getId());
        Recipe latestRecipe = recipeRepository.findLatestByPostId(post.getId()).orElseThrow();

        saveVersion(post.getId(), latestVersion + 1, latestRecipe.getRootRecipeId(),
                latestRecipe.getParentRecipeId(), req, editor.getId());
    }

    private void saveVersion(Long postId, int version, Long rootId, Long parentId, RecipeRequestDto req, Long editorId) {
        // Recipe 정보 저장
        Recipe recipe = Recipe.builder()
                .postId(postId)
                .version(version)
                .rootRecipeId(rootId)
                .parentRecipeId(parentId)
                .title(req.recipeTitle())
                .description(req.description())
                .cookingTime(req.cookingTime())
                .difficulty(req.difficulty().name())
                .build();
        recipeRepository.save(recipe);

        // 재료 및 단계 저장
        for (int i = 0; i < req.ingredients().size(); i++) {
            IngredientRequestDto iDto = req.ingredients().get(i);

            RecipeIngredient ingredient = RecipeIngredient.builder()
                    .postId(postId)
                    .version(version)
                    .name(iDto.name())    // DTO에서 직접 값을 꺼냄
                    .amount(iDto.amount())
                    .type(iDto.type().name()) // Enum을 String으로 변환
                    .displayOrder(i)
                    .build();
            ingredientRepository.save(ingredient);
        }

        // 2. [에러 해결] 조리 단계 저장 로직
        for (StepRequestDto sDto : req.steps()) {
            RecipeStep step = RecipeStep.builder()
                    .postId(postId)
                    .version(version)
                    .stepNumber(sDto.stepNumber()) // DTO에서 직접 값을 꺼냄
                    .description(sDto.description())
                    .imageUrl(sDto.imageUrl())
                    .build();
            stepRepository.save(step);
        }

        RecipeEditLog log = RecipeEditLog.builder()
                .postId(postId)
                .version(version)
                .editorId(editorId)
                .editSummary(req.editSummary())
                .build();

        editLogRepository.save(log);
    }

    @Transactional(readOnly = true)
    public RecipeDetailResponseDto getRecipeDetail(UUID publicId) {
        Post post = postRepository.findByPublicId(publicId)
                .orElseThrow(() -> new IllegalArgumentException("Recipe not found"));

        Recipe recipe = recipeRepository.findLatestByPostId(post.getId())
                .orElseThrow(() -> new IllegalArgumentException("Recipe details not found"));

        RecipeEditLog log = editLogRepository.findByPostIdAndVersion(post.getId(), recipe.getVersion())
                .orElse(null);

        return assembleDetailDto(post, recipe, log);
    }

    private RecipeDetailResponseDto assembleDetailDto(Post post, Recipe recipe, RecipeEditLog log) {
        // 재료 리스트 조회 및 변환
        List<IngredientRequestDto> ingredients = ingredientRepository
                .findAllByPostIdAndVersionOrderByDisplayOrderAsc(recipe.getPostId(), recipe.getVersion())
                .stream()
                .map(i -> new IngredientRequestDto(i.getName(), i.getAmount(), IngredientType.valueOf(i.getType())))
                .toList();

        // 조리 단계 조회 및 변환
        List<StepRequestDto> steps = stepRepository
                .findAllByPostIdAndVersionOrderByStepNumberAsc(recipe.getPostId(), recipe.getVersion())
                .stream()
                .map(s -> new StepRequestDto(s.getStepNumber(), s.getDescription(), s.getImageUrl()))
                .toList();

        // 계층 구조를 위한 PublicId 역조회 (보안 정책 준수)
        UUID rootPublicId = getPublicIdOrNull(recipe.getRootRecipeId());
        UUID parentPublicId = getPublicIdOrNull(recipe.getParentRecipeId());

        return new RecipeDetailResponseDto(
                post.getPublicId(),
                rootPublicId,
                parentPublicId,
                recipe.getVersion(),
                recipe.getTitle(),
                recipe.getDescription(),
                ingredients,
                steps,
                recipe.getCookingTime(),
                recipe.getDifficulty(),
                log != null ? log.getEditSummary() : "Initial Version",
                recipe.getVersionCreatedAt()
        );
    }

    // 내부 ID를 보안용 PublicId로 변환해주는 헬퍼 메서드
    private UUID getPublicIdOrNull(Long internalId) {
        if (internalId == null) return null;
        return postRepository.findById(internalId)
                .map(Post::getPublicId)
                .orElse(null);
    }
}