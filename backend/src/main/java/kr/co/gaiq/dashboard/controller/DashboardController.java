package kr.co.gaiq.dashboard.controller;

import java.util.List;
import kr.co.gaiq.auth.principal.CurrentUserProvider;
import kr.co.gaiq.dashboard.dto.BatchSummaryDto;
import kr.co.gaiq.dashboard.dto.MlConfidenceAlertsDto;
import kr.co.gaiq.dashboard.service.DashboardService;
import kr.co.gaiq.qc.dto.QcMeasurementDto;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Implements "대시보드" tag of {@code 01-gaiq-core.yaml}.
 */
@RestController
@RequestMapping("/api/v1/dashboard")
public class DashboardController {

    private final DashboardService dashboardService;
    private final CurrentUserProvider currentUserProvider;

    public DashboardController(DashboardService dashboardService, CurrentUserProvider currentUserProvider) {
        this.dashboardService = dashboardService;
        this.currentUserProvider = currentUserProvider;
    }

    @GetMapping("/batches/summary")
    public ResponseEntity<BatchSummaryDto> batchSummary() {
        var principal = currentUserProvider.getOrThrow();
        return ResponseEntity.ok(dashboardService.batchSummary(principal));
    }

    @GetMapping("/qc-results/recent")
    public ResponseEntity<List<QcMeasurementDto>> recentQcResults(
            @RequestParam(required = false, defaultValue = "10") int limit) {
        var principal = currentUserProvider.getOrThrow();
        return ResponseEntity.ok(dashboardService.recentQcResults(principal, limit));
    }

    @GetMapping("/ml-confidence-alerts")
    public ResponseEntity<MlConfidenceAlertsDto> mlConfidenceAlerts() {
        return ResponseEntity.ok(dashboardService.mlConfidenceAlerts());
    }
}
