package com.pairingplanet.pairing_planet.repository.comment;

import com.pairingplanet.pairing_planet.domain.entity.comment.Comment;
import com.pairingplanet.pairing_planet.domain.enums.VerdictType;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CommentRepository extends JpaRepository<Comment, Long> {

    Optional<Comment> findByPublicId(UUID publicId);

    // 1. 배댓 조회 (parentId -> parent로 수정 및 Top 3 적용)
    // 필터 없음: 전체 중 좋아요 Top 3
    @Query("SELECT c FROM Comment c WHERE c.postId = :postId AND c.parent IS NULL AND c.isDeleted = false ORDER BY c.likeCount DESC")
    List<Comment> findGlobalBestComments(@Param("postId") Long postId, Pageable pageable);

    // 필터 있음: 특정 Verdict 중 좋아요 Top 3
    @Query("SELECT c FROM Comment c WHERE c.postId = :postId AND c.parent IS NULL AND c.currentVerdict = :verdict AND c.isDeleted = false ORDER BY c.likeCount DESC")
    List<Comment> findFilteredBestComments(@Param("postId") Long postId, @Param("verdict") VerdictType verdict, Pageable pageable);


    // 2. 커서 기반 목록 조회 (parentId -> parent로 수정)
    @Query("""
        SELECT c FROM Comment c 
        WHERE c.postId = :postId 
          AND c.parent IS NULL 
          AND (c.createdAt < :cursorTime OR (c.createdAt = :cursorTime AND c.id < :cursorId))
        ORDER BY c.createdAt DESC, c.id DESC
    """)
    List<Comment> findAllByCursor(
            @Param("postId") Long postId,
            @Param("cursorTime") Instant cursorTime,
            @Param("cursorId") Long cursorId,
            Pageable pageable);

    @Query("""
        SELECT c FROM Comment c 
        WHERE c.postId = :postId 
          AND c.parent IS NULL 
          AND c.currentVerdict = :verdict
          AND (c.createdAt < :cursorTime OR (c.createdAt = :cursorTime AND c.id < :cursorId))
        ORDER BY c.createdAt DESC, c.id DESC
    """)
    List<Comment> findFilteredByCursor(
            @Param("postId") Long postId,
            @Param("verdict") VerdictType verdict,
            @Param("cursorTime") Instant cursorTime,
            @Param("cursorId") Long cursorId,
            Pageable pageable);

    @Modifying
    @Query("UPDATE Comment c SET c.currentVerdict = :newVerdict WHERE c.postId = :postId AND c.userId = :userId")
    void updateVerdictForUserPost(@Param("userId") Long userId, @Param("postId") Long postId, @Param("newVerdict") VerdictType newVerdict);
}