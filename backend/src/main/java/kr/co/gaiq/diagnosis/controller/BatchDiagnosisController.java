package kr.co.gaiq.diagnosis.controller;

import kr.co.gaiq.auth.principal.CurrentUserProvider;
import kr.co.gaiq.diagnosis.dto.BatchDiagnosisDto;
import kr.co.gaiq.diagnosis.service.BatchDiagnosisService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Implements "AI진단" tag of {@code 01-gaiq-core.yaml}.
 */
@RestController
@RequestMapping("/api/v1/batches/{batchId}/diagnosis")
public class BatchDiagnosisController {

    private final BatchDiagnosisService batchDiagnosisService;
    private final CurrentUserProvider currentUserProvider;

    public BatchDiagnosisController(
            BatchDiagnosisService batchDiagnosisService, CurrentUserProvider currentUserProvider) {
        this.batchDiagnosisService = batchDiagnosisService;
        this.currentUserProvider = currentUserProvider;
    }

    @GetMapping
    public ResponseEntity<BatchDiagnosisDto> diagnose(@PathVariable Long batchId) {
        var principal = currentUserProvider.getOrThrow();
        return ResponseEntity.ok(batchDiagnosisService.diagnose(principal, batchId));
    }
}
