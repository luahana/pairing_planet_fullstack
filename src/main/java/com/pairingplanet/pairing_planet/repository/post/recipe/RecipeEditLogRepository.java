package com.pairingplanet.pairing_planet.repository.post.recipe;

import com.pairingplanet.pairing_planet.domain.entity.post.recipe.RecipeEditLog;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface RecipeEditLogRepository extends JpaRepository<RecipeEditLog, Long> {
    // 해당 레시피의 전체 수정 히스토리 조회
    List<RecipeEditLog> findAllByPostIdOrderByVersionDesc(Long postId);

    Optional<RecipeEditLog> findByPostIdAndVersion(Long postId, Integer version);
}