package com.pairingplanet.pairing_planet.repository.context;

import com.pairingplanet.pairing_planet.domain.entity.context.ContextTag;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ContextTagRepository extends JpaRepository<ContextTag, Long> {
    // 특정 디멘션의 모든 태그 조회
    List<ContextTag> findAllByDimensionId(Long dimensionId);

    Optional<ContextTag> findByPublicId(UUID publicId);

    Optional<ContextTag> findFirstByTagName(String tagName);
}