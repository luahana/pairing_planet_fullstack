package com.pairingplanet.pairing_planet.dto.comment;

import com.pairingplanet.pairing_planet.domain.enums.VerdictType;
import java.util.UUID;

/**
 * 댓글 작성 요청 DTO
 */
public record CommentRequestDto(
        Long postId,
        UUID parentPublicId, // [추가] 부모 댓글의 Public ID (UUID)
        String content,
        VerdictType verdict
) {}