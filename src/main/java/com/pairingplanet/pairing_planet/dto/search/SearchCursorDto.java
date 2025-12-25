package com.pairingplanet.pairing_planet.dto.search;

import java.time.Instant;

public record SearchCursorDto(
        Double lastScore,
        Long lastId,
        Instant lastCreatedAt
) {
    // PostgreSQL 안전 최소 날짜 (1970-01-01)
    public static final Instant SAFE_MIN_DATE = Instant.parse("1970-01-01T00:00:00Z");

    // [핵심] 생성자에서 날짜 강제 보정 (이 로직 덕분에 잘못된 날짜는 원천 차단됩니다)
    public SearchCursorDto {
        if (lastCreatedAt == null || lastCreatedAt.isBefore(SAFE_MIN_DATE)) {
            lastCreatedAt = SAFE_MIN_DATE;
        }

        // 점수나 ID가 null인 경우도 방어 (Optional)
        if (lastScore == null) lastScore = Double.MAX_VALUE;
        if (lastId == null) lastId = Long.MAX_VALUE;
    }

    public static SearchCursorDto initial() {
        // 생성자가 알아서 보정하므로 SAFE_MIN_DATE를 넘기든 null을 넘기든 안전함
        return new SearchCursorDto(Double.MAX_VALUE, Long.MAX_VALUE, SAFE_MIN_DATE);
    }
}