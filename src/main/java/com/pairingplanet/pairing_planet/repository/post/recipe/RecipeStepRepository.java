package com.pairingplanet.pairing_planet.repository.post.recipe;

import com.pairingplanet.pairing_planet.domain.entity.post.recipe.RecipeStep;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RecipeStepRepository extends JpaRepository<RecipeStep, Long> {
    // 특정 버전의 모든 조리 단계 조회
    List<RecipeStep> findAllByPostIdAndVersionOrderByStepNumberAsc(Long postId, Integer version);
}