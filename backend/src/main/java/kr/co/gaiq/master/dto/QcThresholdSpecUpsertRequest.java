package kr.co.gaiq.master.dto;

import jakarta.validation.constraints.NotBlank;
import java.math.BigDecimal;
import java.time.LocalDate;

public record QcThresholdSpecUpsertRequest(
        @NotBlank String metricCode,
        @NotBlank String metricName,
        @NotBlank String unit,
        @NotBlank String specType,
        @NotBlank String comparator,
        BigDecimal thresholdValue,
        BigDecimal thresholdValueMax,
        String sourceDoc,
        Long orgId,
        LocalDate effectiveFrom) {
}
