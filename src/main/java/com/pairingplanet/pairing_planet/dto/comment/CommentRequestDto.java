package com.pairingplanet.pairing_planet.dto.comment;

import java.util.UUID;

public record CommentRequestDto(
        UUID postId,
        UUID parentId, // 대댓글일 경우 필수
        String content
) {}