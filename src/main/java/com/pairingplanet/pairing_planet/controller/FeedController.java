package com.pairingplanet.pairing_planet.controller;

import com.pairingplanet.pairing_planet.dto.post.CursorResponse; // [변경]
import com.pairingplanet.pairing_planet.dto.post.PostResponseDto; // [변경]
import com.pairingplanet.pairing_planet.service.FeedService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity; // [추가]
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/posts/feed")
@RequiredArgsConstructor
public class FeedController {

    private final FeedService feedService;

    /**
     * 통합된 CursorResponse 규격으로 메인 피드 반환
     */
    @GetMapping
    public ResponseEntity<CursorResponse<PostResponseDto>> getFeed( // [변경]
                                                                    @AuthenticationPrincipal UUID userId,
                                                                    @RequestParam(required = false, defaultValue = "0") int cursor // FeedService의 offset으로 사용됨
    ) {
        // userId가 null이면 비로그인(게스트) 유저로 서비스에서 처리됨
        CursorResponse<PostResponseDto> response = feedService.getMixedFeed(userId, cursor);
        return ResponseEntity.ok(response);
    }
}