package kr.co.gaiq.ml.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Map;
import kr.co.gaiq.ml.entity.MlModelVersion;

public record MlModelVersionDto(
        Long modelId,
        String targetMetricCode,
        String algorithm,
        String versionNo,
        Instant trainedAt,
        Integer trainingDataCount,
        BigDecimal r2Score,
        BigDecimal mae,
        BigDecimal rmse,
        String kernelType,
        String modelFilePath,
        boolean isActive,
        Map<String, Object> hyperparametersJson) {

    public static MlModelVersionDto from(MlModelVersion m) {
        return new MlModelVersionDto(
                m.getModelId(), m.getTargetMetricCode(), m.getAlgorithm(), m.getVersionNo(), m.getTrainedAt(),
                m.getTrainingDataCount(), m.getR2Score(), m.getMae(), m.getRmse(), m.getKernelType(),
                m.getModelFilePath(), m.isActive(), m.getHyperparametersJson());
    }
}
