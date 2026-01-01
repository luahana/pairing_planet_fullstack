package com.pairingplanet.pairing_planet.repository.post.recipe;

import com.pairingplanet.pairing_planet.domain.entity.post.recipe.RecipePost;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface RecipePostRepository extends JpaRepository<RecipePost, Long> {
    // 보안을 위한 publicId 조회
    Optional<RecipePost> findByPublicId(UUID publicId);
}