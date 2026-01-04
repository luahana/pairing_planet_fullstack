package com.pairingplanet.pairing_planet.service;

import com.pairingplanet.pairing_planet.domain.entity.log_post.LogPost;
import com.pairingplanet.pairing_planet.domain.entity.recipe.Recipe;
import com.pairingplanet.pairing_planet.domain.entity.recipe.RecipeLog;
import com.pairingplanet.pairing_planet.dto.image.ImageResponseDto;
import com.pairingplanet.pairing_planet.dto.log_post.LogPostDetailResponseDto;
import com.pairingplanet.pairing_planet.dto.log_post.CreateLogRequestDto;
import com.pairingplanet.pairing_planet.dto.recipe.RecipeSummaryDto;
import com.pairingplanet.pairing_planet.repository.log_post.LogPostRepository;
import com.pairingplanet.pairing_planet.repository.recipe.RecipeRepository;
import com.pairingplanet.pairing_planet.repository.user.UserRepository;
import com.pairingplanet.pairing_planet.security.UserPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class LogPostService {
    private final LogPostRepository logPostRepository;
    private final RecipeRepository recipeRepository;
    private final ImageService imageService;

    @Value("${file.upload.url-prefix}") // [추가] URL 조합을 위해 필요
    private String urlPrefix;

    public LogPostDetailResponseDto createLog(CreateLogRequestDto req, UserPrincipal principal) {
        Long creatorId = principal.getId();

        // 2. 연결될 레시피를 찾습니다.
        Recipe recipe = recipeRepository.findByPublicId(req.recipePublicId())
                .orElseThrow(() -> new IllegalArgumentException("Recipe not found"));

        LogPost logPost = LogPost.builder()
                .title(req.title())
                .content(req.content())
                .creatorId(creatorId) // 유저 ID 조회 생략
                .locale(recipe.getCulinaryLocale())
                .build();

        // 레시피-로그 연결 정보 생성
        RecipeLog recipeLog = RecipeLog.builder()
                .logPost(logPost)
                .recipe(recipe)
                .rating(req.rating())
                .build();

        logPost.setRecipeLog(recipeLog);
        logPostRepository.save(logPost);

        // 이미지 활성화 (LOG 타입)
        imageService.activateImages(req.imagePublicIds(), logPost);

        return getLogDetail(logPost.getPublicId());
    }

    @Transactional(readOnly = true)
    public LogPostDetailResponseDto getLogDetail(UUID publicId) {
        LogPost logPost = logPostRepository.findByPublicId(publicId)
                .orElseThrow(() -> new IllegalArgumentException("Log not found"));

        RecipeLog recipeLog = logPost.getRecipeLog();
        Recipe linkedRecipe = recipeLog.getRecipe();

        // [수정] 이미지 엔티티 리스트를 ImageResponseDto 리스트로 변환
        List<ImageResponseDto> imageResponses = logPost.getImages().stream()
                .map(img -> new ImageResponseDto(
                        img.getPublicId(),
                        urlPrefix + "/" + img.getStoredFilename()
                ))
                .toList();

        return new LogPostDetailResponseDto(
                logPost.getPublicId(),
                logPost.getTitle(),
                logPost.getContent(),
                recipeLog.getRating(),
                imageResponses, // [적용]
                new RecipeSummaryDto(
                        linkedRecipe.getPublicId(),
                        linkedRecipe.getTitle(),
                        linkedRecipe.getCulinaryLocale(),
                        null,
                        null
                )
        );
    }
}