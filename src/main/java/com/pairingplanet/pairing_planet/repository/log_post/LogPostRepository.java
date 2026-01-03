package com.pairingplanet.pairing_planet.repository.log_post;

import com.pairingplanet.pairing_planet.domain.entity.log_post.LogPost;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface LogPostRepository extends JpaRepository<LogPost, Long> {
    // 1. 상세 조회
    @EntityGraph(attributePaths = {"hashtags", "recipeLog", "recipeLog.recipe"})
    Optional<LogPost> findByPublicId(UUID publicId);

    // 2. 내 로그 목록 (최신순)
    Slice<LogPost> findByCreatorIdAndIsDeletedFalseOrderByCreatedAtDesc(Long creatorId, Pageable pageable);

    // 3. 특정 지역/언어 기반 최신 로그 피드
    Slice<LogPost> findByLocaleAndIsDeletedFalseAndIsPrivateFalseOrderByCreatedAtDesc(String locale, Pageable pageable);
}