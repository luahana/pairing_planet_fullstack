package com.pairingplanet.pairing_planet.domain.entity.post.recipe;

import com.pairingplanet.pairing_planet.domain.entity.post.Post;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import jakarta.persistence.PrimaryKeyJoinColumn;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "recipe_posts")
@PrimaryKeyJoinColumn(name = "post_id")
@DiscriminatorValue("RECIPE")
@Getter @Setter
public class RecipePost extends Post {
    // 공통 필드(publicId, creator 등)는 Post에서 상속받습니다.
}