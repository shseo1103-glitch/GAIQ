package kr.co.gaiq.diagnosis.service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import kr.co.gaiq.auth.principal.UserPrincipal;
import kr.co.gaiq.batch.entity.SynthesisBatch;
import kr.co.gaiq.batch.entity.SynthesisProcessParam;
import kr.co.gaiq.batch.repository.SynthesisProcessParamRepository;
import kr.co.gaiq.batch.service.SynthesisBatchService;
import kr.co.gaiq.diagnosis.dto.BatchDiagnosisDto;
import kr.co.gaiq.diagnosis.dto.BatchDiagnosisDto.ImprovementSuggestion;
import kr.co.gaiq.diagnosis.dto.BatchDiagnosisDto.MetricResult;
import kr.co.gaiq.qc.entity.QcMeasurement;
import kr.co.gaiq.qc.repository.QcMeasurementRepository;
import kr.co.gaiq.qc.service.ThresholdJudgeService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Rule-based diagnosis (grade A/B/C + root-cause text + static improvement mapping) —
 * implements "AI진단" tag of {@code 01-gaiq-core.yaml}. 1단계: ML 기반 추천은 2단계 예정
 * (ai_recipe_recommendation), 여기서는 정적 규칙 매핑만 제공한다.
 */
@Service
public class BatchDiagnosisService {

    /** metric_code(FAIL 발생 시) -> 권장 파라미터/사유 정적 매핑. */
    private static final Map<String, ImprovementSuggestion> STATIC_SUGGESTIONS = Map.of(
            "RAMAN_ID_IG", new ImprovementSuggestion(
                    "GROWTH_TIME_MIN", null, null, "ID/IG 비율 초과(결함도 과다) — 성장시간 단축 또는 챔버온도 하향 권장"),
            "COVERAGE_PCT", new ImprovementSuggestion(
                    "CH4_FLOW_SCCM", null, null, "커버리지 미달 — CH4 유량 증가 또는 성장시간 연장 권장"),
            "CONDUCTIVITY_IMPROVEMENT_PCT", new ImprovementSuggestion(
                    "ANNEAL_TIME_MIN", null, null, "전도도 향상율 미달 — 어닐링 시간 연장 권장"),
            "IACS_PCT", new ImprovementSuggestion(
                    "CHAMBER_TEMP_C", null, null, "도전율 미달 — 챔버 온도 상향 조정 검토"),
            "SURFACE_TEMP_C", new ImprovementSuggestion(
                    "WINDING_SPEED_MH", null, null, "표면온도 기준 초과 — 권취속도 조정 또는 방열설계 검토"));

    private final SynthesisBatchService synthesisBatchService;
    private final QcMeasurementRepository qcMeasurementRepository;
    private final SynthesisProcessParamRepository processParamRepository;
    private final ThresholdJudgeService thresholdJudgeService;

    public BatchDiagnosisService(
            SynthesisBatchService synthesisBatchService,
            QcMeasurementRepository qcMeasurementRepository,
            SynthesisProcessParamRepository processParamRepository,
            ThresholdJudgeService thresholdJudgeService) {
        this.synthesisBatchService = synthesisBatchService;
        this.qcMeasurementRepository = qcMeasurementRepository;
        this.processParamRepository = processParamRepository;
        this.thresholdJudgeService = thresholdJudgeService;
    }

    @Transactional(readOnly = true)
    public BatchDiagnosisDto diagnose(UserPrincipal principal, Long batchId) {
        SynthesisBatch batch = synthesisBatchService.getScoped(principal, batchId);
        List<QcMeasurement> measurements = qcMeasurementRepository.findByBatchIdOrderByMeasuredAtDesc(batchId);

        List<MetricResult> metricResults = new ArrayList<>();
        List<ImprovementSuggestion> suggestions = new ArrayList<>();
        int passCount = 0;
        int judgedCount = 0;

        for (QcMeasurement m : measurements) {
            String judgedResult = m.getJudgedResult();
            String appliedSpecType = m.getJudgedSpecType();
            if (judgedResult == null) {
                // 저장 당시 판정 안 됐다면 현재 스펙으로 재시도
                var judge = thresholdJudgeService.judge(m.getMetricCode(), m.getMeasuredValue(), batch.getOrgId());
                judgedResult = judge.judgedResult();
                appliedSpecType = judge.judgedSpecType();
            }
            metricResults.add(new MetricResult(m.getMetricCode(), m.getMeasuredValue(), judgedResult, appliedSpecType));

            if (judgedResult != null) {
                judgedCount++;
                if ("PASS".equals(judgedResult)) {
                    passCount++;
                } else if (STATIC_SUGGESTIONS.containsKey(m.getMetricCode())) {
                    ImprovementSuggestion base = STATIC_SUGGESTIONS.get(m.getMetricCode());
                    BigDecimal currentParamValue = findLatestParamValue(batchId, base.paramName());
                    suggestions.add(new ImprovementSuggestion(
                            base.paramName(), currentParamValue, base.recommendedValue(), base.rationale()));
                }
            }
        }

        String grade = gradeOf(passCount, judgedCount);
        String rootCauseSummary = buildRootCauseSummary(passCount, judgedCount, metricResults);

        return new BatchDiagnosisDto(batchId, grade, metricResults, rootCauseSummary, suggestions);
    }

    private BigDecimal findLatestParamValue(Long batchId, String paramName) {
        List<SynthesisProcessParam> params =
                processParamRepository.findByBatchIdAndParamNameOrderByRecordedAtAsc(batchId, paramName);
        if (params.isEmpty()) {
            return null;
        }
        return params.get(params.size() - 1).getParamValue();
    }

    private String gradeOf(int passCount, int judgedCount) {
        if (judgedCount == 0) {
            return "C";
        }
        double ratio = (double) passCount / judgedCount;
        if (ratio >= 0.9) {
            return "A";
        } else if (ratio >= 0.6) {
            return "B";
        }
        return "C";
    }

    private String buildRootCauseSummary(int passCount, int judgedCount, List<MetricResult> results) {
        if (judgedCount == 0) {
            return "판정 가능한 QC 측정값이 없습니다. qc_threshold_spec 기준과 매칭되는 metric_code로 측정값을 등록하세요.";
        }
        long failCount = results.stream().filter(r -> "FAIL".equals(r.judgedResult())).count();
        if (failCount == 0) {
            return String.format("전체 %d개 지표 모두 기준을 통과했습니다(PASS %d/%d).", judgedCount, passCount, judgedCount);
        }
        String failedMetrics = results.stream()
                .filter(r -> "FAIL".equals(r.judgedResult()))
                .map(MetricResult::metricCode)
                .distinct()
                .reduce((a, b) -> a + ", " + b)
                .orElse("");
        return String.format(
                "PASS %d/%d — 다음 지표가 기준 미달입니다: %s. 상세 원인 및 개선안은 improvementSuggestions를 참고하세요.",
                passCount, judgedCount, failedMetrics);
    }
}
