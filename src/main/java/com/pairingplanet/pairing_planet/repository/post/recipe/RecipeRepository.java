package com.pairingplanet.pairing_planet.repository.post.recipe;

import com.pairingplanet.pairing_planet.domain.entity.post.recipe.Recipe;
import com.pairingplanet.pairing_planet.domain.entity.post.recipe.RecipeId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface RecipeRepository extends JpaRepository<Recipe, RecipeId> {

    // 특정 포스트의 모든 버전 조회
    List<Recipe> findAllByPostIdOrderByVersionDesc(Long postId);

    // [중요] 특정 포스트의 가장 최신 버전 하나만 조회
    @Query("SELECT r FROM Recipe r WHERE r.postId = :postId AND r.version = " +
            "(SELECT MAX(r2.version) FROM Recipe r2 WHERE r2.postId = :postId)")
    Optional<Recipe> findLatestByPostId(@Param("postId") Long postId);

    // 특정 포스트의 최신 버전 번호만 가져오기 (수정 시 신규 버전 생성용)
    @Query("SELECT COALESCE(MAX(r.version), 0) FROM Recipe r WHERE r.postId = :postId")
    int findMaxVersionByPostId(@Param("postId") Long postId);

    // 오리지널 레시피(Root)를 기준으로 파생된 모든 레시피 조회 (Direct Child 구조)
    List<Recipe> findAllByRootRecipeId(Long rootRecipeId);
}