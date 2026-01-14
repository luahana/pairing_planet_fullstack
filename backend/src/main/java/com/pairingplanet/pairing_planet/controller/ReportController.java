package com.pairingplanet.pairing_planet.controller;

import com.pairingplanet.pairing_planet.dto.report.CreateReportRequest;
import com.pairingplanet.pairing_planet.security.UserPrincipal;
import com.pairingplanet.pairing_planet.service.ReportService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class ReportController {

    private final ReportService reportService;

    /**
     * Report a user for violation
     */
    @PostMapping("/{userId}/report")
    public ResponseEntity<Void> reportUser(
            @PathVariable("userId") UUID targetUserId,
            @Valid @RequestBody CreateReportRequest request,
            @AuthenticationPrincipal UserPrincipal principal) {
        reportService.reportUser(principal.getId(), targetUserId, request);
        return ResponseEntity.ok().build();
    }
}
