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
import kr.co.gaiq.ml.client.MlInferenceClient;
import kr.co.gaiq.ml.client.MlInferenceResponse;
import kr.co.gaiq.ml.dto.MlPredictionCreateRequest;
import kr.co.gaiq.ml.dto.MlPredictionLogDto;
import kr.co.gaiq.ml.entity.MlModelVersion;
import kr.co.gaiq.ml.entity.MlPredictionLog;
import kr.co.gaiq.ml.repository.MlModelVersionRepository;
import kr.co.gaiq.ml.repository.MlPredictionLogRepository;
import kr.co.gaiq.qc.entity.QcMeasurement;
import kr.co.gaiq.qc.repository.QcMeasurementRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Batch-scoped ML prediction requests — implements "ML진단"/"배치이력" tag의 batch-scoped
 * ml-predictions paths of {@code 01-gaiq-core.yaml}.
 *
 * <p>실제 학습된 GPR/RandomForest 모델을 서빙하는 FastAPI 추론 서비스({@code ml/service/main.py},
 * 기본 {@code http://localhost:8115})를 우선 호출한다. 서비스가 다운되었거나 해당
 * target_metric_code에 대해 등록된 활성 모델이 없으면(GPR 예측을 못 받으면) 기존 룰기반
 * 폴백(최근 실측값 → qc_threshold_spec 기준값 → 0)으로 자동 전환한다(graceful degradation).
 */
@Service
public class MlPredictionService {

    private static final Logger log = LoggerFactory.getLogger(MlPredictionService.class);
    private static final String MODULE = "ML_MODEL";

    private final MlModelVersionRepository mlModelVersionRepository;
    private final MlPredictionLogRepository mlPredictionLogRepository;
    private final SynthesisBatchService synthesisBatchService;
    private final SynthesisProcessParamRepository processParamRepository;
    private final QcMeasurementRepository qcMeasurementRepository;
    private final QcThresholdSpecRepository qcThresholdSpecRepository;
    private final AuditRecorder auditRecorder;
    private final MlInferenceClient mlInferenceClient;

    public MlPredictionService(
            MlModelVersionRepository mlModelVersionRepository,
            MlPredictionLogRepository mlPredictionLogRepository,
            SynthesisBatchService synthesisBatchService,
            SynthesisProcessParamRepository processParamRepository,
            QcMeasurementRepository qcMeasurementRepository,
            QcThresholdSpecRepository qcThresholdSpecRepository,
            AuditRecorder auditRecorder,
            MlInferenceClient mlInferenceClient) {
        this.mlModelVersionRepository = mlModelVersionRepository;
        this.mlPredictionLogRepository = mlPredictionLogRepository;
        this.synthesisBatchService = synthesisBatchService;
        this.processParamRepository = processParamRepository;
        this.qcMeasurementRepository = qcMeasurementRepository;
        this.qcThresholdSpecRepository = qcThresholdSpecRepository;
        this.auditRecorder = auditRecorder;
        this.mlInferenceClient = mlInferenceClient;
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
        MlPredictionLog predictionLog = mlPredictionLogRepository.findById(predictionId)
                .filter(p -> p.getBatchId().equals(batchId))
                .orElseThrow(() -> new EntityNotFoundException("MlPredictionLog", predictionId));
        return MlPredictionLogDto.from(predictionLog);
    }

    @Transactional
    public MlPredictionLogDto predict(UserPrincipal principal, Long batchId, MlPredictionCreateRequest request) {
        SynthesisBatch batch = synthesisBatchService.getScoped(principal, batchId);
        String targetMetricCode = request.targetMetricCode();

        Map<String, Object> inputParams = new HashMap<>();
        for (SynthesisProcessParam p : processParamRepository.findByBatchIdOrderByRecordedAtAsc(batchId)) {
            inputParams.put(p.getParamName(), p.getParamValue());
        }

        MlInferenceResponse inference = mlInferenceClient.predict(targetMetricCode, inputParams);

        MlModelVersion model;
        BigDecimal predictedValue;
        BigDecimal predictedStdDev = null;
        String confidenceLevel;

        if (inference != null && inference.hasUsablePrediction()) {
            // FastAPI GPR/RF 서빙 성공 — 실제 학습된 모델의 예측값+표준편차 사용.
            model = mlModelVersionRepository
                    .findFirstByTargetMetricCodeAndActiveTrue(targetMetricCode)
                    .orElseGet(() -> createFallbackModel(targetMetricCode));
            predictedValue = inference.predictedValue();
            predictedStdDev = inference.predictedStdDev();
            confidenceLevel = inference.confidenceLevel();
        } else {
            // FastAPI 다운/타임아웃/모델없음 -> graceful degradation: 기존 룰기반 폴백.
            if (inference == null) {
                log.info("ML inference service unreachable for metric={}, using rule-based fallback", targetMetricCode);
            } else {
                log.info(
                        "ML inference returned no usable prediction for metric={} (algorithm={}, message={}), using rule-based fallback",
                        targetMetricCode, inference.algorithm(), inference.message());
            }
            model = mlModelVersionRepository
                    .findFirstByTargetMetricCodeAndActiveTrue(targetMetricCode)
                    .orElseGet(() -> createFallbackModel(targetMetricCode));
            predictedValue = estimatePredictedValue(batchId, targetMetricCode, batch.getOrgId());
            confidenceLevel = "RULE_BASED_FALLBACK".equals(model.getAlgorithm()) ? "DATA_INSUFFICIENT" : "MEDIUM";
        }

        MlPredictionLog predictionLog = new MlPredictionLog();
        predictionLog.setBatchId(batchId);
        predictionLog.setModelId(model.getModelId());
        predictionLog.setInputParamsJson(inputParams);
        predictionLog.setPredictedValue(predictedValue);
        predictionLog.setPredictedStdDev(predictedStdDev);
        predictionLog.setConfidenceLevel(confidenceLevel);
        predictionLog.setPredictedAt(Instant.now());
        mlPredictionLogRepository.save(predictionLog);

        auditRecorder.recordUserAction(
                batch.getOrgId(), principal.userId(), MODULE, "CREATE", "MlPredictionLog", predictionLog.getPredictionId(),
                null, Map.of("targetMetricCode", targetMetricCode, "algorithm", model.getAlgorithm()));

        return MlPredictionLogDto.from(predictionLog);
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
