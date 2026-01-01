package com.pairingplanet.pairing_planet.repository.post.recipe;

import com.pairingplanet.pairing_planet.domain.entity.post.recipe.RecipeLogPost;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RecipeLogPostRepository extends JpaRepository<RecipeLogPost, Long> {
    // 특정 레시피(target)에 달린 모든 후기 로그 조회
    List<RecipeLogPost> findAllByTargetRecipeIdOrderByCreatedAtDesc(Long targetRecipeId);
}