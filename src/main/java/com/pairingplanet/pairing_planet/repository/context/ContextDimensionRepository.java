package com.pairingplanet.pairing_planet.repository.context;

import com.pairingplanet.pairing_planet.domain.entity.context.ContextDimension;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import java.util.UUID;

public interface ContextDimensionRepository extends JpaRepository<ContextDimension, Long> {
    Optional<ContextDimension> findByPublicId(UUID publicId);
    boolean existsByName(String name);
}