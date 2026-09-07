package kr.co.gaiq.ml.client;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * FastAPI ML 추론 서비스({@code /predict}) 응답 DTO.
 * ml/service/main.py PredictResponse와 1:1 대응.
 */
public record MlInferenceResponse(
        String targetMetricCode,
        String algorithm,
        BigDecimal predictedValue,
        BigDecimal predictedStdDev,
        String confidenceLevel,
        Integer trainingDataCount,
        String modelVersion,
        AuxRandomForest auxRandomForest,
        List<String> missingFeatures,
        String message) {

    public record AuxRandomForest(
            BigDecimal predictedValue, BigDecimal predictedStdDev, Integer trainingDataCount) {
    }

    public boolean hasUsablePrediction() {
        return predictedValue != null && "GPR".equals(algorithm);
    }

    public record Request(String targetMetricCode, Map<String, Object> inputParams) {
    }
}
