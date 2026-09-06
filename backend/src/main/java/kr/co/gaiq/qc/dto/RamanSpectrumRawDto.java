package kr.co.gaiq.qc.dto;

import java.time.Instant;
import java.util.List;
import kr.co.gaiq.qc.entity.RamanSpectrumRaw;

public record RamanSpectrumRawDto(
        Long spectrumId,
        Long batchId,
        Long measurementEquipmentId,
        String scanRangeCm1,
        List<Double> wavenumberArray,
        List<Double> intensityArray,
        String rawFilePath,
        Instant measuredAt) {

    public static RamanSpectrumRawDto from(RamanSpectrumRaw r) {
        return new RamanSpectrumRawDto(
                r.getSpectrumId(), r.getBatchId(), r.getMeasurementEquipmentId(), r.getScanRangeCm1(),
                r.getWavenumberArray(), r.getIntensityArray(), r.getRawFilePath(), r.getMeasuredAt());
    }
}
