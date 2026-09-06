package kr.co.gaiq.batch.dto;

import java.math.BigDecimal;
import java.time.Instant;
import kr.co.gaiq.batch.entity.SynthesisProcessParam;

public record SynthesisProcessParamDto(
        Long paramId,
        Long batchId,
        String paramName,
        BigDecimal paramValue,
        String unit,
        Instant recordedAt) {

    public static SynthesisProcessParamDto from(SynthesisProcessParam p) {
        return new SynthesisProcessParamDto(
                p.getParamId(), p.getBatchId(), p.getParamName(), p.getParamValue(), p.getUnit(), p.getRecordedAt());
    }
}
