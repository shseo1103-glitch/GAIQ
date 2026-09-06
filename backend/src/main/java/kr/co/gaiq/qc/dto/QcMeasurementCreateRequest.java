package kr.co.gaiq.qc.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.Instant;

public record QcMeasurementCreateRequest(
        @NotBlank String metricCode,
        BigDecimal measuredValue,
        @NotBlank String unit,
        Long measurementEquipmentId,
        @NotNull Instant measuredAt,
        String notes) {
}
