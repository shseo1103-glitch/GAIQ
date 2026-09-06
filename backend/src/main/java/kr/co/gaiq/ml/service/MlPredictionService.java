package kr.co.gaiq.ml.service;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import kr.co.gaiq.audit.service.AuditRecorder;
import kr.co.gaiq.auth.principal.UserPrincipal;
import kr.co.gaiq.batch.entity.SynthesisBatch;
import kr.co.gaiq.batch.entity.SynthesisProcessParam;
import kr.co.gaiq.batch.repository.SynthesisProcessParamRepository;
import kr.co.gaiq.batch.service.SynthesisBatchService;
import kr.co.gaiq.common.exception.EntityNotFoundException;
import kr.co.gaiq.master.entity.QcThresholdSpec;
import kr.co.gaiq.master.repository.QcThresholdSpecRepository;
import kr.co.gaiq.ml.dto.MlPredictionCreateRequest;
import kr.co.gaiq.ml.dto.MlPredictionLogDto;
import kr.co.gaiq.ml.entity.MlModelVersion;
import kr.co.gaiq.ml.entity.MlPredictionLog;
import kr.co.gaiq.ml.repository.MlModelVersionRepository;
import kr.co.gaiq.ml.repository.MlPredictionLogRepository;
import kr.co.gaiq.qc.entity.QcMeasurement;
import kr.co.gaiq.qc.repository.QcMeasurementRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Batch-scoped ML prediction requests — implements "ML진단"/"배치이력" tag의 batch-scoped
 * ml-predictions paths of {@code 01-gaiq-core.yaml}.
 *
 * <p>1단계는 실제 학습 파이프라인이 없으므로, 활성 모델이 없으면 RULE_BASED_FALLBACK 모델을
 * 자동 생성/재사용하고 qc_threshold_spec 기준값(또는 최근 실측 평균)을 예측값으로 응답한다.
 */
@Service
public class MlPredictionService {

    private static final String MODULE = "ML_MODEL";

    private final MlModelVersionRepository mlModelVersionRepository;
    private final MlPredictionLogRepository mlPredictionLogRepository;
    private final SynthesisBatchService synthesisBatchService;
    private final SynthesisProcessParamRepository processParamRepository;
    private final QcMeasurementRepository qcMeasurementRepository;
    private final QcThresholdSpecRepository qcThresholdSpecRepository;
    private final AuditRecorder auditRecorder;

    public MlPredictionService(
            MlModelVersionRepository mlModelVersionRepository,
            MlPredictionLogRepository mlPredictionLogRepository,
            SynthesisBatchService synthesisBatchService,
            SynthesisProcessParamRepository processParamRepository,
            QcMeasurementRepository qcMeasurementRepository,
            QcThresholdSpecRepository qcThresholdSpecRepository,
            AuditRecorder auditRecorder) {
        this.mlModelVersionRepository = mlModelVersionRepository;
        this.mlPredictionLogRepository = mlPredictionLogRepository;
        this.synthesisBatchService = synthesisBatchService;
        this.processParamRepository = processParamRepository;
        this.qcMeasurementRepository = qcMeasurementRepository;
        this.qcThresholdSpecRepository = qcThresholdSpecRepository;
        this.auditRecorder = auditRecorder;
    }

    @Transactional(readOnly = true)
    public List<MlPredictionLogDto> listByBatch(UserPrincipal principal, Long batchId) {
        synthesisBatchService.getScoped(principal, batchId);
        return mlPredictionLogRepository.findByBatchIdOrderByPredictedAtDesc(batchId).stream()
                .map(MlPredictionLogDto::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public MlPredictionLogDto getOne(UserPrincipal principal, Long batchId, Long predictionId) {
        synthesisBatchService.getScoped(principal, batchId);
        MlPredictionLog log = mlPredictionLogRepository.findById(predictionId)
                .filter(p -> p.getBatchId().equals(batchId))
                .orElseThrow(() -> new EntityNotFoundException("MlPredictionLog", predictionId));
        return MlPredictionLogDto.from(log);
    }

    @Transactional
    public MlPredictionLogDto predict(UserPrincipal principal, Long batchId, MlPredictionCreateRequest request) {
        SynthesisBatch batch = synthesisBatchService.getScoped(principal, batchId);
        String targetMetricCode = request.targetMetricCode();

        MlModelVersion model = mlModelVersionRepository
                .findFirstByTargetMetricCodeAndActiveTrue(targetMetricCode)
                .orElseGet(() -> createFallbackModel(targetMetricCode));

        Map<String, Object> inputParams = new HashMap<>();
        for (SynthesisProcessParam p : processParamRepository.findByBatchIdOrderByRecordedAtAsc(batchId)) {
            inputParams.put(p.getParamName(), p.getParamValue());
        }

        BigDecimal predictedValue = estimatePredictedValue(batchId, targetMetricCode, batch.getOrgId());
        String confidenceLevel = "RULE_BASED_FALLBACK".equals(model.getAlgorithm()) ? "DATA_INSUFFICIENT" : "MEDIUM";

        MlPredictionLog log = new MlPredictionLog();
        log.setBatchId(batchId);
        log.setModelId(model.getModelId());
        log.setInputParamsJson(inputParams);
        log.setPredictedValue(predictedValue);
        log.setConfidenceLevel(confidenceLevel);
        log.setPredictedAt(Instant.now());
        mlPredictionLogRepository.save(log);

        auditRecorder.recordUserAction(
                batch.getOrgId(), principal.userId(), MODULE, "CREATE", "MlPredictionLog", log.getPredictionId(),
                null, Map.of("targetMetricCode", targetMetricCode, "algorithm", model.getAlgorithm()));

        return MlPredictionLogDto.from(log);
    }

    private MlModelVersion createFallbackModel(String targetMetricCode) {
        MlModelVersion model = new MlModelVersion();
        model.setTargetMetricCode(targetMetricCode);
        model.setAlgorithm("RULE_BASED_FALLBACK");
        model.setVersionNo("fallback-1");
        model.setTrainedAt(Instant.now());
        model.setTrainingDataCount(0);
        model.setModelFilePath("N/A_RULE_BASED_FALLBACK");
        model.setActive(true);
        return mlModelVersionRepository.save(model);
    }

    /** 1단계 단순 추정: 최근 실측 평균 → 없으면 qc_threshold_spec 기준값 → 없으면 0. */
    private BigDecimal estimatePredictedValue(Long batchId, String metricCode, Long orgId) {
        List<QcMeasurement> orgMeasurements = qcMeasurementRepository.findByBatchIdOrderByMeasuredAtDesc(batchId)
                .stream()
                .filter(m -> metricCode.equals(m.getMetricCode()) && m.getMeasuredValue() != null)
                .toList();
        if (!orgMeasurements.isEmpty()) {
            return orgMeasurements.get(0).getMeasuredValue();
        }

        Optional<QcThresholdSpec> spec = qcThresholdSpecRepository.findByMetricCodeAndOrgId(metricCode, orgId)
                .stream()
                .findFirst();
        if (spec.isEmpty()) {
            spec = qcThresholdSpecRepository.findByMetricCodeAndOrgIdIsNull(metricCode).stream().findFirst();
        }
        return spec.map(QcThresholdSpec::getThresholdValue).orElse(BigDecimal.ZERO);
    }
}
