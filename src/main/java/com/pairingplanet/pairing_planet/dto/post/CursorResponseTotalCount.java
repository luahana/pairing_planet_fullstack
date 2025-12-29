package com.pairingplanet.pairing_planet.dto.post;

import java.util.List;

public record CursorResponseTotalCount<T>(
        List<T> data,       // 요구사항의 "data" 필드
        String nextCursor,
        long totalCount     // 요구사항의 "totalCount" 필드
) {}