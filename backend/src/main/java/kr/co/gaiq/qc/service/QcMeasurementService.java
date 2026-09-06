package kr.co.gaiq.qc.service;

import java.util.List;
import java.util.Map;
import kr.co.gaiq.audit.service.AuditRecorder;
import kr.co.gaiq.auth.principal.UserPrincipal;
import kr.co.gaiq.batch.entity.SynthesisBatch;
import kr.co.gaiq.batch.service.SynthesisBatchService;
import kr.co.gaiq.qc.dto.QcMeasurementCreateRequest;
import kr.co.gaiq.qc.dto.QcMeasurementDto;
import kr.co.gaiq.qc.entity.QcMeasurement;
import kr.co.gaiq.qc.repository.QcMeasurementRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * QC measurement CRUD with automatic rule-based judging — implements "품질측정" tag of
 * {@code 01-gaiq-core.yaml}.
 */
@Service
public class QcMeasurementService {

    private static final String MODULE = "QC";

    private final QcMeasurementRepository qcMeasurementRepository;
    private final SynthesisBatchService synthesisBatchService;
    private final ThresholdJudgeService thresholdJudgeService;
    private final AuditRecorder auditRecorder;

    public QcMeasurementService(
            QcMeasurementRepository qcMeasurementRepository,
            SynthesisBatchService synthesisBatchService,
            ThresholdJudgeService thresholdJudgeService,
            AuditRecorder auditRecorder) {
        this.qcMeasurementRepository = qcMeasurementRepository;
        this.synthesisBatchService = synthesisBatchService;
        this.thresholdJudgeService = thresholdJudgeService;
        this.auditRecorder = auditRecorder;
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

        return QcMeasurementDto.from(q);
    }
}
