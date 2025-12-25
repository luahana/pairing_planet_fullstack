package com.pairingplanet.pairing_planet.dto.food;

import java.util.UUID;

public record FoodRequestDto(
        UUID id,
        String name,
        String localeCode // 신규 음식일 경우 필요
) {}