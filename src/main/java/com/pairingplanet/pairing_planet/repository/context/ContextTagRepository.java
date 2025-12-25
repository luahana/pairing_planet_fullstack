package com.pairingplanet.pairing_planet.repository.context;

import com.pairingplanet.pairing_planet.domain.entity.context.ContextTag;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ContextTagRepository extends JpaRepository<ContextTag, Long> {
    List<ContextTag> findAllByLocaleOrderByDisplayOrderAsc(String locale);

    // [변경] 특정 디멘션 내에서 순서대로 조회
    List<ContextTag> findAllByDimensionIdAndLocaleOrderByDisplayOrderAsc(Long dimensionId, String locale);

    Optional<ContextTag> findByPublicId(UUID publicId);
}