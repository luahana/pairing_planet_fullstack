package com.pairingplanet.pairing_planet.dto.food;

import com.fasterxml.jackson.annotation.JsonCreator;

import java.util.UUID;

public record FoodRequestDto(
        UUID id,
        String name,
        String localeCode // 신규 음식일 경우 필요
) {
    // [수정] mode = JsonCreator.Mode.DELEGATING 을 추가해야
    // "qwer" 같은 단일 문자열을 객체로 변환할 수 있습니다.
    @JsonCreator(mode = JsonCreator.Mode.DELEGATING)
    public static FoodRequestDto fromString(String name) {
        // 서비스 로직과의 일관성을 위해 기본 로케일을 "ko"로 설정하는 것을 추천합니다.
        return new FoodRequestDto(null, name, "ko");
    }
}