package com.pairingplanet.pairing_planet.service;

import com.pairingplanet.pairing_planet.domain.entity.comment.Comment;
import com.pairingplanet.pairing_planet.domain.entity.post.Post;
import com.pairingplanet.pairing_planet.domain.entity.user.User;
import com.pairingplanet.pairing_planet.domain.entity.verdict.PostVerdict;
import com.pairingplanet.pairing_planet.domain.entity.verdict.PostVerdictId;
import com.pairingplanet.pairing_planet.domain.enums.VerdictType;
import com.pairingplanet.pairing_planet.dto.comment.CommentListResponseDto;
import com.pairingplanet.pairing_planet.dto.comment.CommentRequestDto;
import com.pairingplanet.pairing_planet.dto.comment.CommentResponseDto;
import com.pairingplanet.pairing_planet.repository.comment.CommentRepository;
import com.pairingplanet.pairing_planet.repository.post.PostRepository;
import com.pairingplanet.pairing_planet.repository.user.UserRepository;
import com.pairingplanet.pairing_planet.repository.verdict.PostVerdictRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CommentService {

    private final CommentRepository commentRepository;
    private final PostVerdictRepository verdictRepository;
    private final UserRepository userRepository; // [추가]
    private final PostRepository postRepository; // [추가]

    private static final Instant SAFE_MIN_DATE = Instant.parse("1970-01-01T00:00:00Z");
    private static final Instant SAFE_MAX_DATE = Instant.parse("3000-01-01T00:00:00Z"); // 안전한 최대값

    // 댓글 작성
    @Transactional
    public void createComment(UUID userId, CommentRequestDto request) { // [변경] Long -> UUID
        // 1. UUID -> Long 변환
        User user = userRepository.findByPublicId(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        // request.postId()도 UUID일 것이므로 변환 필요
        // DTO의 postId 타입을 UUID로 변경했다고 가정하거나, String이라면 변환
        UUID postPublicId = request.postId(); // (가정: DTO도 UUID로 수정됨)
        Post post = postRepository.findByPublicId(postPublicId)
                .orElseThrow(() -> new IllegalArgumentException("Post not found"));

        // 2. Verdict 조회 (복합키: Long userId, Long postId)
        PostVerdict postVerdict = verdictRepository.findById(new PostVerdictId(user.getId(), post.getId()))
                .orElse(null);

        VerdictType currentType = (postVerdict != null) ? postVerdict.getVerdictType() : null;

        // 부모 댓글 처리 (Optional)
        Long parentId = null;
        if (request.parentId() != null) {
            // request.parentId()는 UUID여야 함
            Comment parent = commentRepository.findByPublicId(request.parentId())
                    .orElseThrow(() -> new IllegalArgumentException("Parent comment not found"));
            parentId = parent.getId();
        }

        Comment comment = Comment.builder()
                .postId(post.getId()) // 내부 Long ID 저장
                .userId(user.getId()) // 내부 Long ID 저장
                .parentId(parentId)
                .content(request.content())
                .initialVerdict(currentType)
                .currentVerdict(currentType)
                .build();

        commentRepository.save(comment);
    }

    // Verdict 변경 시 (내부 로직이므로 Long 사용 가능하나, 컨트롤러 호출이면 UUID 변환 필요)
    @Transactional
    public void onVerdictSwitched(Long userId, Long postId, VerdictType newType) {
        // 내부 이벤트나 스케줄러에서 호출된다면 Long 유지 가능
        // 만약 컨트롤러에서 호출된다면 위 createComment처럼 변환 로직 추가
        commentRepository.updateVerdictForUserPost(userId, postId, newType);
    }

    // 댓글 목록 조회
    @Transactional(readOnly = true)
    public CommentListResponseDto getComments(UUID userId, UUID postId, VerdictType filterType, String cursor) {
        // 1. UUID -> Long 변환
        // (비로그인 허용 시 userId null 체크)
        Long internalUserId = null;
        if (userId != null) {
            internalUserId = userRepository.findByPublicId(userId)
                    .map(User::getId)
                    .orElse(null);
        }

        Post post = postRepository.findByPublicId(postId)
                .orElseThrow(() -> new IllegalArgumentException("Post not found"));
        Long internalPostId = post.getId();

        // 2. 배댓 가져오기 (Long ID 사용)
        List<Comment> bestEntities;
        if (filterType == null) {
            bestEntities = commentRepository.findGlobalBestComments(internalPostId);
        } else {
            bestEntities = commentRepository.findFilteredBestComments(internalPostId, filterType);
        }

        // 3. 커서 파싱 (UUID 포함된 커서 해독)
        Instant cursorTime;
        Long cursorInternalId;

        if (cursor == null) {
            cursorTime = SAFE_MAX_DATE;
            cursorInternalId = Long.MAX_VALUE;
        } else {
            // 커서 포맷: "2025-01-01T..._UUID"
            String[] parts = cursor.split("_");
            try {
                cursorTime = Instant.parse(parts[0]);
                // [추가] 안전장치: 너무 작은 값은 1970년으로 보정
                if (cursorTime.isBefore(SAFE_MIN_DATE)) {
                    cursorTime = SAFE_MIN_DATE;
                }
            } catch (Exception e) {
                cursorTime = SAFE_MAX_DATE; // 파싱 실패 시 안전값
            }
            UUID cursorPublicId = UUID.fromString(parts[1]);

            // UUID -> Long 변환
            cursorInternalId = commentRepository.findByPublicId(cursorPublicId)
                    .map(Comment::getId)
                    .orElse(0L);
        }

        // 4. 리스트 가져오기 (Long ID 사용)
        int fetchSize = 10;
        List<Comment> listEntities;

        if (filterType == null) {
            listEntities = commentRepository.findAllByCursor(internalPostId, cursorTime, cursorInternalId, PageRequest.of(0, fetchSize));
        } else {
            listEntities = commentRepository.findFilteredByCursor(internalPostId, filterType, cursorTime, cursorInternalId, PageRequest.of(0, fetchSize));
        }

        // 5. DTO 변환 (내부 ID 숨기고 Public ID 노출)
        // (CommentResponseDto.from 메서드도 Public ID를 쓰도록 되어있어야 함)
        List<CommentResponseDto> bestDtos = bestEntities.stream()
                .map(c -> CommentResponseDto.from(c, false))
                .toList();

        List<CommentResponseDto> listDtos = listEntities.stream()
                .map(c -> CommentResponseDto.from(c, false))
                .toList();

        // 6. 다음 커서 생성 (Public ID 사용)
        String nextCursor = null;
        if (!listEntities.isEmpty()) {
            Comment last = listEntities.get(listEntities.size() - 1);
            nextCursor = last.getCreatedAt().toString() + "_" + last.getPublicId();
        }

        return new CommentListResponseDto(bestDtos, listDtos, nextCursor, !listEntities.isEmpty());
    }
}