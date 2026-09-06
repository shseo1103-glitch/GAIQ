package kr.co.gaiq.dashboard.service;

import java.util.List;
import kr.co.gaiq.auth.principal.UserPrincipal;
import kr.co.gaiq.batch.repository.SynthesisBatchRepository;
import kr.co.gaiq.dashboard.dto.BatchSummaryDto;
import kr.co.gaiq.dashboard.dto.MlConfidenceAlertsDto;
import kr.co.gaiq.ml.repository.MlPredictionLogRepository;
import kr.co.gaiq.qc.dto.QcMeasurementDto;
import kr.co.gaiq.qc.repository.QcMeasurementRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Dashboard widgets — implements "대시보드" tag of {@code 01-gaiq-core.yaml}. Org-scoped for
 * non-PLATFORM_ADMIN callers; PLATFORM_ADMIN sees platform-wide aggregates.
 */
@Service
public class DashboardService {

    private final SynthesisBatchRepository synthesisBatchRepository;
    private final QcMeasurementRepository qcMeasurementRepository;
    private final MlPredictionLogRepository mlPredictionLogRepository;

    public DashboardService(
            SynthesisBatchRepository synthesisBatchRepository,
            QcMeasurementRepository qcMeasurementRepository,
            MlPredictionLogRepository mlPredictionLogRepository) {
        this.synthesisBatchRepository = synthesisBatchRepository;
        this.qcMeasurementRepository = qcMeasurementRepository;
        this.mlPredictionLogRepository = mlPredictionLogRepository;
    }

    @Transactional(readOnly = true)
    public BatchSummaryDto batchSummary(UserPrincipal principal) {
        if (principal.isPlatformAdmin()) {
            return new BatchSummaryDto(
                    synthesisBatchRepository.countByStatus("PLANNED"),
                    synthesisBatchRepository.countByStatus("RUNNING"),
                    synthesisBatchRepository.countByStatus("COMPLETED"),
                    synthesisBatchRepository.countByStatus("FAILED"));
        }
        Long orgId = principal.orgId();
        return new BatchSummaryDto(
                synthesisBatchRepository.countByOrgIdAndStatus(orgId, "PLANNED"),
                synthesisBatchRepository.countByOrgIdAndStatus(orgId, "RUNNING"),
                synthesisBatchRepository.countByOrgIdAndStatus(orgId, "COMPLETED"),
                synthesisBatchRepository.countByOrgIdAndStatus(orgId, "FAILED"));
    }

    @Transactional(readOnly = true)
    public List<QcMeasurementDto> recentQcResults(UserPrincipal principal, int limit) {
        var batchIds = principal.isPlatformAdmin()
                ? synthesisBatchRepository.findAll().stream().map(b -> b.getBatchId()).toList()
                : synthesisBatchRepository.findByOrgId(principal.orgId(), PageRequest.of(0, 1000))
                        .getContent().stream().map(b -> b.getBatchId()).toList();
        if (batchIds.isEmpty()) {
            return List.of();
        }
        return qcMeasurementRepository
                .findByBatchIdInOrderByMeasuredAtDesc(batchIds, PageRequest.of(0, limit, Sort.by("measuredAt").descending()))
                .stream()
                .map(QcMeasurementDto::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public MlConfidenceAlertsDto mlConfidenceAlerts() {
        return new MlConfidenceAlertsDto(
                mlPredictionLogRepository.countByConfidenceLevel("LOW"),
                mlPredictionLogRepository.countByConfidenceLevel("DATA_INSUFFICIENT"));
    }
}
