package com.pairingplanet.pairing_planet.dto.user;

import com.pairingplanet.pairing_planet.domain.enums.Gender;
import java.time.LocalDate;
import java.util.UUID;

public record UpdateProfileRequestDto(
        String username,
        UUID profileImagePublicId, // [수정] String profileImageUrl -> UUID profileImagePublicId
        Gender gender,
        LocalDate birthDate,
        UUID preferredDietaryId,
        Boolean marketingAgreed,
        String locale,  // 언어 설정: ko-KR, en-US
        String defaultFoodStyle  // 기본 요리 스타일: ISO country code (e.g., "KR", "US")
) {}