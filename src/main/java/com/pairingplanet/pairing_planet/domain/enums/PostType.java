package com.pairingplanet.pairing_planet.domain.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum PostType {
    DAILY("daily_logs"),
    REVIEW("reviews"),
    RECIPE("recipes");

    private final String description;
}