package kr.co.gaiq.batch.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.Instant;

public record SynthesisBatchCreateRequest(
        @NotBlank String batchNo,
        @NotNull Long equipmentModelId,
        @NotNull Long substrateId,
        Long rawMaterialId,
        @NotBlank String processType,
        Instant startedAt,
        String recipeVersion,
        String notes) {
}
