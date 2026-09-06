package kr.co.gaiq.master.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import kr.co.gaiq.master.entity.QcThresholdSpec;

public record QcThresholdSpecDto(
        Long thresholdId,
        String metricCode,
        String metricName,
        String unit,
        String specType,
        String comparator,
        BigDecimal thresholdValue,
        BigDecimal thresholdValueMax,
        String sourceDoc,
        Long orgId,
        LocalDate effectiveFrom,
        Instant createdAt,
        Instant updatedAt) {

    public static QcThresholdSpecDto from(QcThresholdSpec q) {
        return new QcThresholdSpecDto(
                q.getThresholdId(), q.getMetricCode(), q.getMetricName(), q.getUnit(), q.getSpecType(),
                q.getComparator(), q.getThresholdValue(), q.getThresholdValueMax(), q.getSourceDoc(), q.getOrgId(),
                q.getEffectiveFrom(), q.getCreatedAt(), q.getUpdatedAt());
    }
}
