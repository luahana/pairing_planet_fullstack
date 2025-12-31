package com.pairingplanet.pairing_planet.domain.entity.context;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;

@Entity
@Table(name = "context_tags")
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
    private String tagName; // 시스템 코드

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "display_names", nullable = false)
    private Map<String, String> displayNames;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "display_orders", nullable = false)
    private Map<String, Integer> displayOrders;

    @Builder.Default
    private Instant createdAt = Instant.now();

    // 특정 로케일의 표시 이름을 가져오는 편의 메서드
    public String getDisplayNameByLocale(String locale) {
        return displayNames.getOrDefault(locale, displayNames.get("en-US"));
    }

    // 특정 로케일의 순서를 가져오는 편의 메서드
    public Integer getOrderByLocale(String locale) {
        return displayOrders.getOrDefault(locale, 0);
    }
}