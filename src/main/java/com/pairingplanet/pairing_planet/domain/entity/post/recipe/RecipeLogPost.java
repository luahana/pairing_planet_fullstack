package com.pairingplanet.pairing_planet.domain.entity.post.recipe;

import com.pairingplanet.pairing_planet.domain.entity.post.Post;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "recipe_logs")
@PrimaryKeyJoinColumn(name = "post_id")
@DiscriminatorValue("RECIPE_LOG")
@Getter
@Setter
@NoArgsConstructor
public class RecipeLogPost extends Post {

    @Column(name = "target_recipe_id", nullable = false)
    private Long targetRecipeId; // 어떤 레시피를 보고 만들었는가

    private Integer rating; // 별점 (1-5)
}