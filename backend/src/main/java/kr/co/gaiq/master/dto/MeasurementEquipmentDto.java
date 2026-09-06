package kr.co.gaiq.master.dto;

import java.time.Instant;
import java.time.LocalDate;
import kr.co.gaiq.master.entity.MeasurementEquipment;

public record MeasurementEquipmentDto(
        Long measurementEquipmentId,
        String equipmentCode,
        String equipmentName,
        String equipmentType,
        String manufacturer,
        LocalDate calibrationDueDate,
        Instant createdAt,
        Instant updatedAt) {

    public static MeasurementEquipmentDto from(MeasurementEquipment m) {
        return new MeasurementEquipmentDto(
                m.getMeasurementEquipmentId(), m.getEquipmentCode(), m.getEquipmentName(), m.getEquipmentType(),
                m.getManufacturer(), m.getCalibrationDueDate(), m.getCreatedAt(), m.getUpdatedAt());
    }
}
