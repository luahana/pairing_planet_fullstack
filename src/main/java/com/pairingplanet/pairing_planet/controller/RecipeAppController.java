package com.pairingplanet.pairing_planet.controller;

import com.pairingplanet.pairing_planet.dto.recipe.*;
import com.pairingplanet.pairing_planet.dto.log_post.*;
import com.pairingplanet.pairing_planet.security.UserPrincipal;
import com.pairingplanet.pairing_planet.service.RecipeService;
import com.pairingplanet.pairing_planet.service.LogPostService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class RecipeAppController {

    private final RecipeService recipeService;
    private final LogPostService logPostService;

    // --- [TAB 1: HOME] ---
    /**
     * 홈 피드: 최근 레시피 및 활발한 변형 트리 조회
     */
    @GetMapping("/home")
    public ResponseEntity<HomeFeedResponseDto> getHomeFeed() {
        return ResponseEntity.ok(recipeService.getHomeFeed());
    }

    // --- [TAB 2: RECIPES] ---
    /**
     * 레시피 탐색: 오리지널(Root) 레시피 리스트 조회
     */
    @GetMapping("/recipes")
    public ResponseEntity<Slice<RecipeSummaryDto>> getRootRecipes(
            @RequestParam String locale,
            Pageable pageable) {
        return ResponseEntity.ok(recipeService.findRootRecipes(locale, pageable));
    }

    /**
     * 레시피 상세: 상단에 루트 레시피 고정 + 변형 리스트 + 로그 포함
     */
    @GetMapping("/recipes/{publicId}")
    public ResponseEntity<RecipeDetailResponseDto> getRecipeDetail(@PathVariable UUID publicId) {
        return ResponseEntity.ok(recipeService.getRecipeDetail(publicId));
    }

    // --- [TAB 3: CREATE (+)] ---
    /**
     * 새 레시피 등록 (오리지널 또는 기존 레시피로부터의 변형 생성)
     */
    @PostMapping("/recipes")
    public ResponseEntity<RecipeDetailResponseDto> createRecipe(
            @RequestBody CreateRecipeRequestDto req,
            @AuthenticationPrincipal UserPrincipal principal) { // JWT에서 유저 정보 추출
        return ResponseEntity.ok(recipeService.createRecipe(req, principal));
    }

    /**
     * 새 로그 등록: 레시피를 만들어 본 경험 기록
     */
    @PostMapping("/logs")
    public ResponseEntity<LogPostDetailResponseDto> createLog(
            @RequestBody CreateLogRequestDto req,
            @AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(logPostService.createLog(req, principal));
    }

    // --- [LOG DETAIL] ---
    /**
     * 로그 상세: 사진, 메모, 연결된 레시피 카드 포함
     */
    @GetMapping("/logs/{publicId}")
    public ResponseEntity<LogPostDetailResponseDto> getLogDetail(@PathVariable UUID publicId) {
        return ResponseEntity.ok(logPostService.getLogDetail(publicId));
    }
}