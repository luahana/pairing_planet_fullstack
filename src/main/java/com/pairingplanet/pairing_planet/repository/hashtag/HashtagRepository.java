package com.pairingplanet.pairing_planet.repository.hashtag;

import com.pairingplanet.pairing_planet.domain.entity.hashtag.Hashtag;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface HashtagRepository extends JpaRepository<Hashtag, Long> {
    Optional<Hashtag> findByName(String name);
}