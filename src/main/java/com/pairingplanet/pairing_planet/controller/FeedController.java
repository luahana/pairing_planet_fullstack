package com.pairingplanet.pairing_planet.controller;

import com.pairingplanet.pairing_planet.dto.feed.FeedResponseDto;
import com.pairingplanet.pairing_planet.service.FeedService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID; // [필수]

@RestController
@RequestMapping("/api/v1/posts/feed")
@RequiredArgsConstructor
public class FeedController {

    private final FeedService feedService;

    @GetMapping
    public FeedResponseDto getFeed(
            @AuthenticationPrincipal UUID userId, // [변경]
            @RequestParam(required = false, defaultValue = "0") int cursor
    ) {
        // 비로그인 유저(-1L 등) 처리를 어떻게 할지 결정 필요.
        // UUID는 -1을 가질 수 없으므로, null이면 비로그인으로 처리하도록 Service를 수정해야 함.
        return feedService.getMixedFeed(userId, cursor);
    }
}