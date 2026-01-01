package com.pairingplanet.pairing_planet.domain.entity.post.recipe;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Entity
@Table(name = "recipes")
@IdClass(RecipeId.class)
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Recipe {

    @Id
    @Column(name = "post_id")
    private Long postId;

    @Id
    private Integer version;

    @Column(name = "root_recipe_id")
    private Long rootRecipeId; // 오리지널 레시피 ID

    @Column(name = "parent_recipe_id")
    private Long parentRecipeId; // 직전 부모 레시피 ID

    @Column(nullable = false)
    private String title;

    private String description;

    @Column(name = "cooking_time")
    private Integer cookingTime;

    private String difficulty; // EASY, NORMAL, HARD

    @Column(name = "version_created_at")
    @Builder.Default
    private Instant versionCreatedAt = Instant.now(); // 버전 생성 시점
}