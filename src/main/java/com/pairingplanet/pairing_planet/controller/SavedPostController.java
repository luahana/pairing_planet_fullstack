package com.pairingplanet.pairing_planet.controller;

import com.pairingplanet.pairing_planet.dto.post.CursorResponse;
import com.pairingplanet.pairing_planet.dto.post.PostResponseDto;
import com.pairingplanet.pairing_planet.service.SavedPostService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/posts/saved")
@RequiredArgsConstructor
public class SavedPostController {

    private final SavedPostService savedPostService;

    @PostMapping("/{postId}")
    public ResponseEntity<Map<String, Boolean>> toggleSave(
            @AuthenticationPrincipal UUID userId, // [참고] SecurityContext에 UUID가 저장되어 있어야 함
            @PathVariable UUID postId
    ) {
        boolean isSaved = savedPostService.toggleSave(userId, postId);
        return ResponseEntity.ok(Map.of("isSaved", isSaved));
    }

    @GetMapping
    public ResponseEntity<CursorResponse<PostResponseDto>> getMySavedPosts(
            @AuthenticationPrincipal UUID userId,
            @RequestParam(required = false) String cursor,
            @RequestParam(name = "limit", defaultValue = "10") int limit
    ) {
        // [수정] 서비스의 변경된 반환 타입과 일치
        return ResponseEntity.ok(savedPostService.getSavedPosts(userId, cursor, limit));
    }
}