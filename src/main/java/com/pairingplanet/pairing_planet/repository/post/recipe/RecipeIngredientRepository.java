package com.pairingplanet.pairing_planet.repository.post.recipe;

import com.pairingplanet.pairing_planet.domain.entity.post.recipe.RecipeIngredient;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RecipeIngredientRepository extends JpaRepository<RecipeIngredient, Long> {
    // 특정 버전의 모든 재료 조회
    List<RecipeIngredient> findAllByPostIdAndVersionOrderByDisplayOrderAsc(Long postId, Integer version);
}