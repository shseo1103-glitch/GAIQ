package kr.co.gaiq.master.dto;

import jakarta.validation.constraints.NotBlank;
import java.util.Map;

public record EquipmentModelUpsertRequest(
        @NotBlank String modelCode,
        @NotBlank String modelName,
        @NotBlank String equipmentType,
        @NotBlank String processType,
        String manufacturer,
        Map<String, Object> specJson) {
}
