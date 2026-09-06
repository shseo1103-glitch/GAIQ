package kr.co.gaiq.ml.controller;

import jakarta.validation.Valid;
import java.util.List;
import kr.co.gaiq.auth.principal.CurrentUserProvider;
import kr.co.gaiq.ml.dto.MlPredictionCreateRequest;
import kr.co.gaiq.ml.dto.MlPredictionLogDto;
import kr.co.gaiq.ml.service.MlPredictionService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Implements "ML진단"/"배치이력" tag의 batch-scoped ml-predictions paths of
 * {@code 01-gaiq-core.yaml}.
 */
@RestController
@RequestMapping("/api/v1/batches/{batchId}/ml-predictions")
public class MlPredictionController {

    private final MlPredictionService mlPredictionService;
    private final CurrentUserProvider currentUserProvider;

    public MlPredictionController(MlPredictionService mlPredictionService, CurrentUserProvider currentUserProvider) {
        this.mlPredictionService = mlPredictionService;
        this.currentUserProvider = currentUserProvider;
    }

    @GetMapping
    public ResponseEntity<List<MlPredictionLogDto>> list(@PathVariable Long batchId) {
        var principal = currentUserProvider.getOrThrow();
        return ResponseEntity.ok(mlPredictionService.listByBatch(principal, batchId));
    }

    @PostMapping
    public ResponseEntity<MlPredictionLogDto> predict(
            @PathVariable Long batchId, @Valid @RequestBody MlPredictionCreateRequest request) {
        var principal = currentUserProvider.getOrThrow();
        return ResponseEntity.status(HttpStatus.CREATED).body(mlPredictionService.predict(principal, batchId, request));
    }

    @GetMapping("/{predictionId}")
    public ResponseEntity<MlPredictionLogDto> getOne(
            @PathVariable Long batchId, @PathVariable Long predictionId) {
        var principal = currentUserProvider.getOrThrow();
        return ResponseEntity.ok(mlPredictionService.getOne(principal, batchId, predictionId));
    }
}
