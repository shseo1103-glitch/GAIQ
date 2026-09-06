package kr.co.gaiq.master.dto;

import java.math.BigDecimal;
import java.time.Instant;
import kr.co.gaiq.master.entity.RawMaterial;

public record RawMaterialDto(
        Long rawMaterialId,
        String materialCode,
        String materialName,
        String materialType,
        BigDecimal purityPct,
        String lotNo,
        String supplier,
        Instant createdAt,
        Instant updatedAt) {

    public static RawMaterialDto from(RawMaterial r) {
        return new RawMaterialDto(
                r.getRawMaterialId(), r.getMaterialCode(), r.getMaterialName(), r.getMaterialType(),
                r.getPurityPct(), r.getLotNo(), r.getSupplier(), r.getCreatedAt(), r.getUpdatedAt());
    }
}
