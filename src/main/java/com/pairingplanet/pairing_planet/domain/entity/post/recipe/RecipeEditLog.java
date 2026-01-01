package com.pairingplanet.pairing_planet.domain.entity.post.recipe;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Entity
@Table(name = "recipe_edit_logs")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RecipeEditLog {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "post_id", nullable = false)
    private Long postId;

    @Column(nullable = false)
    private Integer version;

    @Column(name = "editor_id", nullable = false)
    private Long editorId;

    @Column(name = "edit_summary", nullable = false, columnDefinition = "TEXT")
    private String editSummary; // 수정 요약

    @Column(name = "created_at")
    @Builder.Default
    private Instant createdAt = Instant.now();
}