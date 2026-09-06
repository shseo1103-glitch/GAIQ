package kr.co.gaiq.master.dto;

import java.time.Instant;
import java.util.Map;
import kr.co.gaiq.master.entity.EquipmentModel;

public record EquipmentModelDto(
        Long equipmentModelId,
        String modelCode,
        String modelName,
        String equipmentType,
        String processType,
        String manufacturer,
        Map<String, Object> specJson,
        Instant createdAt,
        Instant updatedAt) {

    public static EquipmentModelDto from(EquipmentModel e) {
        return new EquipmentModelDto(
                e.getEquipmentModelId(), e.getModelCode(), e.getModelName(), e.getEquipmentType(),
                e.getProcessType(), e.getManufacturer(), e.getSpecJson(), e.getCreatedAt(), e.getUpdatedAt());
    }
}
