package kr.co.gaiq.master.dto;

import jakarta.validation.constraints.NotBlank;
import java.math.BigDecimal;

public record RawMaterialUpsertRequest(
        @NotBlank String materialCode,
        @NotBlank String materialName,
        @NotBlank String materialType,
        BigDecimal purityPct,
        String lotNo,
        String supplier) {
}
