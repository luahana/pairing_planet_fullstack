package com.pairingplanet.pairing_planet.dto.report;

import com.pairingplanet.pairing_planet.domain.enums.ReportReason;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CreateReportRequest(
        @NotNull(message = "Report reason is required")
        ReportReason reason,

        @Size(max = 1000, message = "Description must not exceed 1000 characters")
        String description
) {}
