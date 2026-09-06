package kr.co.gaiq.batch.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.Instant;

public record SynthesisProcessParamCreateRequest(
        @NotBlank String paramName,
        @NotNull BigDecimal paramValue,
        @NotBlank String unit,
        Instant recordedAt) {
}
