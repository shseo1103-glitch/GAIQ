package kr.co.gaiq.master.dto;

import jakarta.validation.constraints.NotBlank;
import java.time.LocalDate;

public record MeasurementEquipmentUpsertRequest(
        @NotBlank String equipmentCode,
        @NotBlank String equipmentName,
        @NotBlank String equipmentType,
        String manufacturer,
        LocalDate calibrationDueDate) {
}
