package com.pairingplanet.pairing_planet.domain.entity.context;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "context_dimensions")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class ContextDimension {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "public_id", nullable = false, unique = true, updatable = false)
    @Builder.Default
    private UUID publicId = UUID.randomUUID();

    @Column(nullable = false, unique = true, length = 50)
    private String name;

    @OneToMany(mappedBy = "dimension", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<ContextTag> tags = new ArrayList<>();

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private Instant updatedAt;
}