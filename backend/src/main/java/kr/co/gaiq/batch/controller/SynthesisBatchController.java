package kr.co.gaiq.batch.controller;

import jakarta.validation.Valid;
import java.time.LocalDate;
import java.util.List;
import kr.co.gaiq.auth.principal.CurrentUserProvider;
import kr.co.gaiq.batch.dto.BatchStatusUpdateRequest;
import kr.co.gaiq.batch.dto.ProcessParamBatchCreateRequest;
import kr.co.gaiq.batch.dto.SynthesisBatchCreateRequest;
import kr.co.gaiq.batch.dto.SynthesisBatchDto;
import kr.co.gaiq.batch.dto.SynthesisProcessParamDto;
import kr.co.gaiq.batch.service.SynthesisBatchService;
import kr.co.gaiq.common.dto.PageResponse;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Implements "공정배치"/"배치이력" tags of {@code 01-gaiq-core.yaml}.
 */
@RestController
@RequestMapping("/api/v1/batches")
public class SynthesisBatchController {

    private final SynthesisBatchService synthesisBatchService;
    private final CurrentUserProvider currentUserProvider;

    public SynthesisBatchController(
            SynthesisBatchService synthesisBatchService, CurrentUserProvider currentUserProvider) {
        this.synthesisBatchService = synthesisBatchService;
        this.currentUserProvider = currentUserProvider;
    }

    @GetMapping
    public ResponseEntity<PageResponse<SynthesisBatchDto>> list(
            @RequestParam(required = false) String batchNo,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) LocalDate startDate,
            @RequestParam(required = false) LocalDate endDate,
            @RequestParam(required = false) Long orgId,
            Pageable pageable) {
        var principal = currentUserProvider.getOrThrow();
        return ResponseEntity.ok(
                synthesisBatchService.list(principal, orgId, batchNo, status, startDate, endDate, pageable));
    }

    @PostMapping
    public ResponseEntity<SynthesisBatchDto> create(@Valid @RequestBody SynthesisBatchCreateRequest request) {
        var principal = currentUserProvider.getOrThrow();
        return ResponseEntity.status(HttpStatus.CREATED).body(synthesisBatchService.create(principal, request));
    }

    @GetMapping("/{batchId}")
    public ResponseEntity<SynthesisBatchDto> get(@PathVariable Long batchId) {
        var principal = currentUserProvider.getOrThrow();
        return ResponseEntity.ok(synthesisBatchService.get(principal, batchId));
    }

    @PatchMapping("/{batchId}/status")
    public ResponseEntity<SynthesisBatchDto> updateStatus(
            @PathVariable Long batchId, @Valid @RequestBody BatchStatusUpdateRequest request) {
        var principal = currentUserProvider.getOrThrow();
        return ResponseEntity.ok(synthesisBatchService.updateStatus(principal, batchId, request));
    }

    @GetMapping("/{batchId}/process-params")
    public ResponseEntity<List<SynthesisProcessParamDto>> listProcessParams(
            @PathVariable Long batchId, @RequestParam(required = false) String paramName) {
        var principal = currentUserProvider.getOrThrow();
        return ResponseEntity.ok(synthesisBatchService.listProcessParams(principal, batchId, paramName));
    }

    @PostMapping("/{batchId}/process-params")
    public ResponseEntity<List<SynthesisProcessParamDto>> createProcessParams(
            @PathVariable Long batchId, @Valid @RequestBody ProcessParamBatchCreateRequest request) {
        var principal = currentUserProvider.getOrThrow();
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(synthesisBatchService.createProcessParams(principal, batchId, request));
    }
}
