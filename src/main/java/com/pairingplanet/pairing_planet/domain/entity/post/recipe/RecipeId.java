package com.pairingplanet.pairing_planet.domain.entity.post.recipe;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RecipeId implements Serializable {
    private Long postId;
    private Integer version;
}