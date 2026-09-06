package kr.co.gaiq.ml.controller;

import java.util.List;
import kr.co.gaiq.common.dto.PageResponse;
import kr.co.gaiq.ml.dto.MlModelVersionDto;
import kr.co.gaiq.ml.dto.MlPredictionLogDto;
import kr.co.gaiq.ml.service.MlModelService;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Implements "ML진단" tag's model-scoped paths of {@code 01-gaiq-core.yaml}.
 */
@RestController
@RequestMapping("/api/v1/ml-models")
public class MlModelController {

    private final MlModelService mlModelService;

    public MlModelController(MlModelService mlModelService) {
        this.mlModelService = mlModelService;
    }

    @GetMapping
    public ResponseEntity<List<MlModelVersionDto>> list(
            @RequestParam(required = false) String targetMetricCode,
            @RequestParam(required = false, defaultValue = "true") boolean activeOnly) {
        return ResponseEntity.ok(mlModelService.list(targetMetricCode, activeOnly));
    }

    @GetMapping("/{modelId}")
    public ResponseEntity<MlModelVersionDto> get(@PathVariable Long modelId) {
        return ResponseEntity.ok(mlModelService.get(modelId));
    }

    @GetMapping("/{modelId}/predictions")
    public ResponseEntity<PageResponse<MlPredictionLogDto>> listPredictions(
            @PathVariable Long modelId, Pageable pageable) {
        return ResponseEntity.ok(mlModelService.listPredictions(modelId, pageable));
    }
}
