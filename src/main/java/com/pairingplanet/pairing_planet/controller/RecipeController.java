package com.pairingplanet.pairing_planet.controller;

import com.pairingplanet.pairing_planet.dto.post.recipe.RecipeDetailResponseDto;
import com.pairingplanet.pairing_planet.dto.post.recipe.RecipeRequestDto;
import com.pairingplanet.pairing_planet.service.RecipeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/recipes")
@RequiredArgsConstructor
public class RecipeController {
    private final RecipeService recipeService;

    // 오리지널 레시피 작성
    @PostMapping
    public ResponseEntity<RecipeDetailResponseDto> create(
            @AuthenticationPrincipal UUID userId,
            @RequestBody @Valid RecipeRequestDto request) {
        return ResponseEntity.ok(recipeService.saveRecipe(userId, request, null));
    }

    // 변형 레시피 작성 (sourceId 기반)
    @PostMapping("/derive/{sourcePublicId}")
    public ResponseEntity<RecipeDetailResponseDto> derive(
            @AuthenticationPrincipal UUID userId,
            @PathVariable UUID sourcePublicId,
            @RequestBody @Valid RecipeRequestDto request) {
        return ResponseEntity.ok(recipeService.saveRecipe(userId, request, sourcePublicId));
    }

    // 레시피 새 버전 등록 (수정 이력 생성)
    @PostMapping("/{publicId}/versions")
    public ResponseEntity<Void> addNewVersion(
            @PathVariable UUID publicId,
            @AuthenticationPrincipal UUID userId,
            @RequestBody @Valid RecipeRequestDto request) {
        recipeService.createNewVersion(publicId, userId, request);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    // 레시피 상세 조회
    @GetMapping("/{publicId}")
    public ResponseEntity<RecipeDetailResponseDto> getDetail(@PathVariable UUID publicId) {
        return ResponseEntity.ok(recipeService.getRecipeDetail(publicId));
    }
}