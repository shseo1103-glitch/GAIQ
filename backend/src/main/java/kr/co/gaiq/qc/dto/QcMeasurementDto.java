package kr.co.gaiq.qc.dto;

import java.math.BigDecimal;
import java.time.Instant;
import kr.co.gaiq.qc.entity.QcMeasurement;

public record QcMeasurementDto(
        Long qcMeasurementId,
        Long batchId,
        String metricCode,
        BigDecimal measuredValue,
        String unit,
        Long measurementEquipmentId,
        Instant measuredAt,
        String judgedResult,
        String judgedSpecType,
        Long measuredByUserId,
        String notes) {

    public static QcMeasurementDto from(QcMeasurement q) {
        return new QcMeasurementDto(
                q.getQcMeasurementId(), q.getBatchId(), q.getMetricCode(), q.getMeasuredValue(), q.getUnit(),
                q.getMeasurementEquipmentId(), q.getMeasuredAt(), q.getJudgedResult(), q.getJudgedSpecType(),
                q.getMeasuredByUserId(), q.getNotes());
    }
}
