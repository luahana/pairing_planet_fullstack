package com.pairingplanet.pairing_planet.domain.entity.context;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "context_tags", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"dimension_id", "tag_name", "locale"})
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class ContextTag {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "public_id", nullable = false, unique = true, updatable = false)
    @Builder.Default
    private UUID publicId = UUID.randomUUID();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "dimension_id", nullable = false)
    private ContextDimension dimension;

    @Column(name = "tag_name", nullable = false, length = 50)
    private String tagName;       // 시스템 내부 코드용 (예: "christmas")

    @Column(name = "display_name", nullable = false, length = 50)
    private String displayName;   // 사용자 표시용 (예: "Christmas" or "크리스마스")

    @Column(nullable = false, length = 10)
    private String locale;

    @Column(name = "display_order", nullable = false)
    @Builder.Default
    private Integer displayOrder = 0; // 기본값 0

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private Instant updatedAt;
}