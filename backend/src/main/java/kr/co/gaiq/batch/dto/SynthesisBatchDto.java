package kr.co.gaiq.batch.dto;

import java.time.Instant;
import kr.co.gaiq.batch.entity.SynthesisBatch;

public record SynthesisBatchDto(
        Long batchId,
        Long orgId,
        String batchNo,
        Long equipmentModelId,
        Long substrateId,
        Long rawMaterialId,
        String processType,
        Instant startedAt,
        Instant endedAt,
        String status,
        Long operatorUserId,
        String recipeVersion,
        String notes,
        Instant createdAt,
        Instant updatedAt) {

    public static SynthesisBatchDto from(SynthesisBatch b) {
        return new SynthesisBatchDto(
                b.getBatchId(), b.getOrgId(), b.getBatchNo(), b.getEquipmentModelId(), b.getSubstrateId(),
                b.getRawMaterialId(), b.getProcessType(), b.getStartedAt(), b.getEndedAt(), b.getStatus(),
                b.getOperatorUserId(), b.getRecipeVersion(), b.getNotes(), b.getCreatedAt(), b.getUpdatedAt());
    }
}
