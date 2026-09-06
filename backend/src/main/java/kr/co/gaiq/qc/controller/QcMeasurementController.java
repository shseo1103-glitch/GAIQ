package kr.co.gaiq.qc.controller;

import jakarta.validation.Valid;
import java.util.List;
import kr.co.gaiq.auth.principal.CurrentUserProvider;
import kr.co.gaiq.qc.dto.QcMeasurementCreateRequest;
import kr.co.gaiq.qc.dto.QcMeasurementDto;
import kr.co.gaiq.qc.service.QcMeasurementService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Implements "품질측정" tag's qc-measurements paths of {@code 01-gaiq-core.yaml}.
 */
@RestController
@RequestMapping("/api/v1/batches/{batchId}/qc-measurements")
public class QcMeasurementController {

    private final QcMeasurementService qcMeasurementService;
    private final CurrentUserProvider currentUserProvider;

    public QcMeasurementController(QcMeasurementService qcMeasurementService, CurrentUserProvider currentUserProvider) {
        this.qcMeasurementService = qcMeasurementService;
        this.currentUserProvider = currentUserProvider;
    }

    @GetMapping
    public ResponseEntity<List<QcMeasurementDto>> list(@PathVariable Long batchId) {
        var principal = currentUserProvider.getOrThrow();
        return ResponseEntity.ok(qcMeasurementService.list(principal, batchId));
    }

    @PostMapping
    public ResponseEntity<QcMeasurementDto> create(
            @PathVariable Long batchId, @Valid @RequestBody QcMeasurementCreateRequest request) {
        var principal = currentUserProvider.getOrThrow();
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(qcMeasurementService.create(principal, batchId, request));
    }
}
