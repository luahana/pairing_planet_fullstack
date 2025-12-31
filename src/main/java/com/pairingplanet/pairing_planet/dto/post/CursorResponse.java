package com.pairingplanet.pairing_planet.dto.post;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record CursorResponse<T>(
        List<T> data,
        String nextCursor,
        Boolean hasNext,
        Long totalCount
) {
    // totalCount가 필요 없는 경우를 위한 생성자
    public CursorResponse(List<T> data, String nextCursor, boolean hasNext) {
        this(data, nextCursor, hasNext, null);
    }
}