package kr.co.gaiq.qc.service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import kr.co.gaiq.master.entity.QcThresholdSpec;
import kr.co.gaiq.master.repository.QcThresholdSpecRepository;
import org.springframework.stereotype.Service;

/**
 * Rule-based judging: compares a measured value against {@code qc_threshold_spec} rows
 * (org-specific spec takes priority over platform-common spec; among spec_types, the most
 * "strict"/applicable one available is used — 1단계에서는 TARGET_STAGE2 > TARGET_STAGE1 >
 * VALIDATED_ACHIEVEMENT 순으로 우선 적용).
 */
@Service
public class ThresholdJudgeService {

    private static final List<String> SPEC_TYPE_PRIORITY =
            List.of("TARGET_STAGE2", "TARGET_STAGE1", "VALIDATED_ACHIEVEMENT");

    private final QcThresholdSpecRepository qcThresholdSpecRepository;

    public ThresholdJudgeService(QcThresholdSpecRepository qcThresholdSpecRepository) {
        this.qcThresholdSpecRepository = qcThresholdSpecRepository;
    }

    public record JudgeResult(String judgedResult, String judgedSpecType, QcThresholdSpec appliedSpec) {
        static JudgeResult none() {
            return new JudgeResult(null, null, null);
        }
    }

    public JudgeResult judge(String metricCode, BigDecimal measuredValue, Long orgId) {
        if (measuredValue == null) {
            return JudgeResult.none();
        }
        Optional<QcThresholdSpec> spec = resolveApplicableSpec(metricCode, orgId);
        if (spec.isEmpty()) {
            return JudgeResult.none();
        }
        boolean pass = compare(measuredValue, spec.get());
        return new JudgeResult(pass ? "PASS" : "FAIL", spec.get().getSpecType(), spec.get());
    }

    /** org 전용 스펙 우선, 없으면 플랫폼 공통(orgId NULL) 스펙에서 spec_type 우선순위대로 탐색. */
    public Optional<QcThresholdSpec> resolveApplicableSpec(String metricCode, Long orgId) {
        List<QcThresholdSpec> orgSpecific = orgId != null
                ? qcThresholdSpecRepository.findByMetricCodeAndOrgId(metricCode, orgId)
                : List.of();
        List<QcThresholdSpec> common = qcThresholdSpecRepository.findByMetricCodeAndOrgIdIsNull(metricCode);

        for (String specType : SPEC_TYPE_PRIORITY) {
            Optional<QcThresholdSpec> found = orgSpecific.stream()
                    .filter(s -> specType.equals(s.getSpecType()))
                    .findFirst();
            if (found.isPresent()) {
                return found;
            }
        }
        for (String specType : SPEC_TYPE_PRIORITY) {
            Optional<QcThresholdSpec> found = common.stream()
                    .filter(s -> specType.equals(s.getSpecType()))
                    .findFirst();
            if (found.isPresent()) {
                return found;
            }
        }
        return Optional.empty();
    }

    private boolean compare(BigDecimal value, QcThresholdSpec spec) {
        String comparator = spec.getComparator();
        BigDecimal threshold = spec.getThresholdValue();
        return switch (comparator) {
            case "<=" -> threshold != null && value.compareTo(threshold) <= 0;
            case ">=" -> threshold != null && value.compareTo(threshold) >= 0;
            case "=" -> threshold != null && value.compareTo(threshold) == 0;
            case "RANGE" -> threshold != null && spec.getThresholdValueMax() != null
                    && value.compareTo(threshold) >= 0 && value.compareTo(spec.getThresholdValueMax()) <= 0;
            default -> false;
        };
    }
}
