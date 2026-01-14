package com.pairingplanet.pairing_planet.dto.block;

import java.util.List;

public record BlockedUsersListResponse(
        List<BlockedUserDto> content,
        boolean hasNext,
        int page,
        int size,
        long totalElements
) {}
