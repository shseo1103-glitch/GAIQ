package kr.co.gaiq.diagnosis.dto;

import java.math.BigDecimal;
import java.util.List;

public record BatchDiagnosisDto(
        Long batchId,
        String grade,
        List<MetricResult> metricResults,
        String rootCauseSummary,
        List<ImprovementSuggestion> improvementSuggestions) {

    public record MetricResult(
            String metricCode, BigDecimal measuredValue, String judgedResult, String appliedSpecType) {
    }

    public record ImprovementSuggestion(
            String paramName, BigDecimal currentValue, BigDecimal recommendedValue, String rationale) {
    }
}
