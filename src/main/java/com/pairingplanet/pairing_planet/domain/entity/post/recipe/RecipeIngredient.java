package com.pairingplanet.pairing_planet.domain.entity.post.recipe;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "recipe_ingredients")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RecipeIngredient {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long postId;
    private Integer version;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(length = 50)
    private String amount;

    @Column(length = 20)
    private String type; // MAIN, SEASONING 등

    @Column(name = "display_order")
    private Integer displayOrder;
}