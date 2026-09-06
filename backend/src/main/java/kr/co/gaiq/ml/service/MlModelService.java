package kr.co.gaiq.ml.service;

import java.util.List;
import kr.co.gaiq.common.dto.PageResponse;
import kr.co.gaiq.common.exception.EntityNotFoundException;
import kr.co.gaiq.ml.dto.MlModelVersionDto;
import kr.co.gaiq.ml.dto.MlPredictionLogDto;
import kr.co.gaiq.ml.entity.MlModelVersion;
import kr.co.gaiq.ml.repository.MlModelVersionRepository;
import kr.co.gaiq.ml.repository.MlPredictionLogRepository;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * ML model version listing/detail + per-model prediction log listing — implements "ML진단" tag
 * (model-scoped paths) of {@code 01-gaiq-core.yaml}.
 */
@Service
public class MlModelService {

    private final MlModelVersionRepository mlModelVersionRepository;
    private final MlPredictionLogRepository mlPredictionLogRepository;

    public MlModelService(
            MlModelVersionRepository mlModelVersionRepository, MlPredictionLogRepository mlPredictionLogRepository) {
        this.mlModelVersionRepository = mlModelVersionRepository;
        this.mlPredictionLogRepository = mlPredictionLogRepository;
    }

    @Transactional(readOnly = true)
    public List<MlModelVersionDto> list(String targetMetricCode, boolean activeOnly) {
        List<MlModelVersion> models;
        if (targetMetricCode != null && activeOnly) {
            models = mlModelVersionRepository.findByTargetMetricCodeAndActiveTrue(targetMetricCode);
        } else if (targetMetricCode != null) {
            models = mlModelVersionRepository.findByTargetMetricCode(targetMetricCode);
        } else {
            models = mlModelVersionRepository.findAll();
        }
        return models.stream().map(MlModelVersionDto::from).toList();
    }

    @Transactional(readOnly = true)
    public MlModelVersionDto get(Long modelId) {
        return MlModelVersionDto.from(getOrThrow(modelId));
    }

    @Transactional(readOnly = true)
    public PageResponse<MlPredictionLogDto> listPredictions(Long modelId, Pageable pageable) {
        getOrThrow(modelId);
        return PageResponse.of(
                mlPredictionLogRepository.findByModelIdOrderByPredictedAtDesc(modelId, pageable),
                MlPredictionLogDto::from);
    }

    MlModelVersion getOrThrow(Long modelId) {
        return mlModelVersionRepository.findById(modelId)
                .orElseThrow(() -> new EntityNotFoundException("MlModelVersion", modelId));
    }
}
