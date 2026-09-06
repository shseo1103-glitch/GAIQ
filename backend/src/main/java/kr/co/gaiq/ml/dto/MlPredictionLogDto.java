package kr.co.gaiq.ml.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Map;
import kr.co.gaiq.ml.entity.MlPredictionLog;

public record MlPredictionLogDto(
        Long predictionId,
        Long batchId,
        Long modelId,
        Map<String, Object> inputParamsJson,
        BigDecimal predictedValue,
        BigDecimal predictedStdDev,
        String confidenceLevel,
        BigDecimal actualValue,
        BigDecimal residual,
        Instant predictedAt) {

    public static MlPredictionLogDto from(MlPredictionLog p) {
        return new MlPredictionLogDto(
                p.getPredictionId(), p.getBatchId(), p.getModelId(), p.getInputParamsJson(), p.getPredictedValue(),
                p.getPredictedStdDev(), p.getConfidenceLevel(), p.getActualValue(), p.getResidual(),
                p.getPredictedAt());
    }
}
