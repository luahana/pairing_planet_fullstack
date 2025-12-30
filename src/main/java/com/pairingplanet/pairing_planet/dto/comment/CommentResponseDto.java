package com.pairingplanet.pairing_planet.dto.comment;

import com.pairingplanet.pairing_planet.domain.entity.comment.Comment;
import com.pairingplanet.pairing_planet.domain.entity.user.User;
import com.pairingplanet.pairing_planet.domain.enums.VerdictType;
import com.pairingplanet.pairing_planet.dto.user.UserDto;

import java.time.Instant;
import java.util.UUID;

public record CommentResponseDto(
        UUID id,
        UUID parentPublicId,
        String content,
        VerdictType currentVerdict,
        boolean isSwitched,
        int likeCount,
        boolean isLikedByMe,
        Instant createdAt,
        UserDto writer
) {
    /**
     * Comment 엔티티를 CommentResponseDto로 변환합니다.
     * @param c 댓글 엔티티
     * @param writer 작성자 엔티티
     * @param isLikedByMe 현재 로그인한 유저의 좋아요 여부
     * @param urlPrefix 프로필 이미지 경로 구성을 위한 프리픽스
     * @param writerDietaryUuid 작성자의 식이 취향 Public ID (UserDto 시그니처 대응)
     */
    public static CommentResponseDto from(
            Comment c,
            User writer,
            boolean isLikedByMe,
            String urlPrefix,
            UUID writerDietaryUuid
    ) {
        boolean switched = c.getInitialVerdict() != c.getCurrentVerdict();

        // 부모 댓글이 존재할 경우 부모의 Public ID 추출
        UUID parentId = (c.getParent() != null) ? c.getParent().getPublicId() : null;

        return new CommentResponseDto(
                c.getPublicId(),
                parentId, // [수정] 대댓글 구분을 위해 부모 ID 매핑
                c.isDeleted() ? "삭제된 댓글입니다." : c.getContent(),
                c.getCurrentVerdict(),
                switched,
                c.getLikeCount(),
                isLikedByMe,
                c.getCreatedAt(),
                // [수정] UserDto.from의 새로운 시그니처(성별, 생년월일, 취향 포함) 호출
                UserDto.from(writer, urlPrefix, writerDietaryUuid)
        );
    }
}