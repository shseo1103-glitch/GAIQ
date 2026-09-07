package kr.co.gaiq.qc.service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import kr.co.gaiq.audit.service.AuditRecorder;
import kr.co.gaiq.auth.principal.UserPrincipal;
import kr.co.gaiq.batch.entity.SynthesisBatch;
import kr.co.gaiq.batch.service.SynthesisBatchService;
import kr.co.gaiq.ml.entity.MlModelVersion;
import kr.co.gaiq.ml.entity.MlPredictionLog;
import kr.co.gaiq.ml.repository.MlModelVersionRepository;
import kr.co.gaiq.ml.repository.MlPredictionLogRepository;
import kr.co.gaiq.qc.dto.QcMeasurementCreateRequest;
import kr.co.gaiq.qc.dto.QcMeasurementDto;
import kr.co.gaiq.qc.entity.QcMeasurement;
import kr.co.gaiq.qc.repository.QcMeasurementRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * QC measurement CRUD with automatic rule-based judging — implements "품질측정" tag of
 * {@code 01-gaiq-core.yaml}.
 *
 * <p>QC 실측값이 저장되면, 같은 배치에 있는 대상(target_metric_code 일치) ML 예측 로그 중 아직
 * actual_value가 채워지지 않은 것을 찾아 actualValue/residual을 자동 갱신한다(예측-실측 자동 매칭).
 * ML 진단 리포트(SCR-08)의 예측 vs 실측 산점도/잔차 차트가 이 값을 사용한다.
 */
@Service
public class QcMeasurementService {

    private static final Logger log = LoggerFactory.getLogger(QcMeasurementService.class);
    private static final String MODULE = "QC";

    private final QcMeasurementRepository qcMeasurementRepository;
    private final SynthesisBatchService synthesisBatchService;
    private final ThresholdJudgeService thresholdJudgeService;
    private final AuditRecorder auditRecorder;
    private final MlModelVersionRepository mlModelVersionRepository;
    private final MlPredictionLogRepository mlPredictionLogRepository;

    public QcMeasurementService(
            QcMeasurementRepository qcMeasurementRepository,
            SynthesisBatchService synthesisBatchService,
            ThresholdJudgeService thresholdJudgeService,
            AuditRecorder auditRecorder,
            MlModelVersionRepository mlModelVersionRepository,
            MlPredictionLogRepository mlPredictionLogRepository) {
        this.qcMeasurementRepository = qcMeasurementRepository;
        this.synthesisBatchService = synthesisBatchService;
        this.thresholdJudgeService = thresholdJudgeService;
        this.auditRecorder = auditRecorder;
        this.mlModelVersionRepository = mlModelVersionRepository;
        this.mlPredictionLogRepository = mlPredictionLogRepository;
    }

    @Transactional(readOnly = true)
    public List<QcMeasurementDto> list(UserPrincipal principal, Long batchId) {
        synthesisBatchService.getScoped(principal, batchId);
        return qcMeasurementRepository.findByBatchIdOrderByMeasuredAtDesc(batchId).stream()
                .map(QcMeasurementDto::from)
                .toList();
    }

    @Transactional
    public QcMeasurementDto create(UserPrincipal principal, Long batchId, QcMeasurementCreateRequest request) {
        SynthesisBatch batch = synthesisBatchService.getScoped(principal, batchId);

        QcMeasurement q = new QcMeasurement();
        q.setBatchId(batchId);
        q.setMetricCode(request.metricCode());
        q.setMeasuredValue(request.measuredValue());
        q.setUnit(request.unit());
        q.setMeasurementEquipmentId(request.measurementEquipmentId());
        q.setMeasuredAt(request.measuredAt());
        q.setMeasuredByUserId(principal.userId());
        q.setNotes(request.notes());

        ThresholdJudgeService.JudgeResult judge =
                thresholdJudgeService.judge(request.metricCode(), request.measuredValue(), batch.getOrgId());
        q.setJudgedResult(judge.judgedResult());
        q.setJudgedSpecType(judge.judgedSpecType());

        qcMeasurementRepository.save(q);

        auditRecorder.recordUserAction(
                batch.getOrgId(), principal.userId(), MODULE, "CREATE", "QcMeasurement", q.getQcMeasurementId(),
                null, Map.of("metricCode", q.getMetricCode(), "judgedResult", String.valueOf(q.getJudgedResult())));

        backfillMlPredictionActuals(batchId, request.metricCode(), request.measuredValue());

        return QcMeasurementDto.from(q);
    }

    /**
     * 이 QC 실측값과 같은 배치의 대상 metric을 가지는 ML 예측 로그(actual_value 미확정) 전량을 찾아
     * actualValue/residual을 채운다. 한 배치에 동일 metric으로 여러 번 예측했던 경우 전량 업데이트.
     */
    private void backfillMlPredictionActuals(Long batchId, String metricCode, BigDecimal measuredValue) {
        if (measuredValue == null) {
            return;
        }
        List<MlModelVersion> models = mlModelVersionRepository.findByTargetMetricCode(metricCode);
        if (models.isEmpty()) {
            return;
        }
        List<Long> modelIds = models.stream().map(MlModelVersion::getModelId).toList();
        List<MlPredictionLog> pending =
                mlPredictionLogRepository.findByBatchIdAndModelIdInAndActualValueIsNull(batchId, modelIds);
        for (MlPredictionLog p : pending) {
            p.setActualValue(measuredValue);
            p.setResidual(p.getPredictedValue().subtract(measuredValue));
        }
        if (!pending.isEmpty()) {
            mlPredictionLogRepository.saveAll(pending);
            log.info("Backfilled actualValue/residual for {} ML prediction(s) on batch={}, metric={}",
                    pending.size(), batchId, metricCode);
        }
    }
}
