package com.cookstemma.cookstemma.controller;

import com.cookstemma.cookstemma.dto.bot.BotLoginRequestDto;
import com.cookstemma.cookstemma.dto.bot.BotLoginResponseDto;
import com.cookstemma.cookstemma.service.BotAuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controller for bot authentication.
 * Provides API key-based login for bot users.
 */
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class BotAuthController {

    private final BotAuthService botAuthService;

    /**
     * Authenticates a bot using an API key.
     * Returns JWT tokens for subsequent API calls.
     *
     * POST /api/v1/auth/bot-login
     * {
     *   "apiKey": "pp_bot_..."
     * }
     */
    @PostMapping("/bot-login")
    public ResponseEntity<BotLoginResponseDto> botLogin(
            @RequestBody @Valid BotLoginRequestDto request) {
        return ResponseEntity.ok(botAuthService.loginWithApiKey(request));
    }
}
