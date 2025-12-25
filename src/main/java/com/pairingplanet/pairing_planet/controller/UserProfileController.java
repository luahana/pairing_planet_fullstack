package com.pairingplanet.pairing_planet.controller;

import com.pairingplanet.pairing_planet.dto.post.CursorResponse;
import com.pairingplanet.pairing_planet.dto.post.MyPostResponseDto;
import com.pairingplanet.pairing_planet.service.UserProfileService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID; // [필수]

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserProfileController {

    private final UserProfileService userProfileService;

    @GetMapping("/{userId}/posts")
    public ResponseEntity<CursorResponse<MyPostResponseDto>> getUserPosts(
            @PathVariable UUID userId, // [변경]
            @RequestParam(required = false) String cursor,
            @RequestParam(defaultValue = "10") int size
    ) {
        CursorResponse<MyPostResponseDto> response = userProfileService.getOtherUserPosts(userId, cursor, size);
        return ResponseEntity.ok(response);
    }
}