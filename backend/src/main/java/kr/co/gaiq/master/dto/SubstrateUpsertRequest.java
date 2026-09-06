package kr.co.gaiq.master.dto;

import jakarta.validation.constraints.NotBlank;
import java.math.BigDecimal;

public record SubstrateUpsertRequest(
        @NotBlank String substrateCode,
        @NotBlank String substrateType,
        String specDesignation,
        BigDecimal crossSectionMm2,
        BigDecimal diameterMm,
        Integer strandCount) {
}
