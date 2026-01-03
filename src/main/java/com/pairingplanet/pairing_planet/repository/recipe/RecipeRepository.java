package com.pairingplanet.pairing_planet.repository.recipe;

import com.pairingplanet.pairing_planet.domain.entity.recipe.Recipe;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface RecipeRepository extends JpaRepository<Recipe, Long> {
    // 1. 상세 조회 (계보 정보와 해시태그를 함께 로드)
    @EntityGraph(attributePaths = {"rootRecipe", "parentRecipe", "hashtags"})
    Optional<Recipe> findByPublicId(UUID publicId);

    // 2. 전체 피드 조회 (공개된 최신 레시피 slice)
    @Query("SELECT r FROM Recipe r WHERE r.isDeleted = false AND r.isPrivate = false")
    Slice<Recipe> findPublicRecipes(Pageable pageable);

    // 3. 특정 레시피로부터 파생된 변형 레시피 목록 조회
    List<Recipe> findByParentRecipeIdAndIsDeletedFalse(Long parentId);

    // 4. 특정 루트 레시피를 공유하는 모든 계보 조회
    List<Recipe> findByRootRecipeIdAndIsDeletedFalse(Long rootId);

    // 5. 유저별 레시피 개수 (통계용)
    long countByCreatorIdAndIsDeletedFalse(Long creatorId);

    @Query("""
        SELECT r FROM Recipe r 
        LEFT JOIN Recipe v ON v.rootRecipe = r 
        WHERE r.rootRecipe IS NULL 
        GROUP BY r.id 
        ORDER BY MAX(v.createdAt) DESC
    """)
    List<Recipe> findTrendingVariantTrees(Pageable pageable);

    // 3. 특정 계보 전체 조회 (Root부터 모든 자식까지)
    List<Recipe> findAllByRootRecipeIdOrIdOrderByCreatedAtDesc(Long rootId, Long id);

    @Query("SELECT r FROM Recipe r WHERE r.rootRecipe IS NULL AND r.culinaryLocale = :locale AND r.isDeleted = false")
    Slice<Recipe> findRootRecipesByLocale(@Param("locale") String locale, Pageable pageable);

    // Home: 최근 생성된 레시피 5개 조회
    List<Recipe> findTop5ByIsDeletedFalseOrderByCreatedAtDesc();

    // Home: 최근 변형이 일어난 오리지널 레시피들 조회 (활발한 트리용)
    @Query("""
        SELECT r FROM Recipe r 
        LEFT JOIN Recipe v ON v.rootRecipe = r 
        WHERE r.rootRecipe IS NULL AND r.isDeleted = false
        GROUP BY r.id 
        ORDER BY MAX(v.createdAt) DESC
    """)
    List<Recipe> findTrendingOriginals(Pageable pageable);

    /**
     * [해결] 특정 루트 레시피를 기준으로 생성된 모든 변형 레시피의 개수 조회
     * SQL: SELECT COUNT(*) FROM recipes WHERE root_recipe_id = ? AND is_deleted = false
     */
    long countByRootRecipeIdAndIsDeletedFalse(Long rootId);
}