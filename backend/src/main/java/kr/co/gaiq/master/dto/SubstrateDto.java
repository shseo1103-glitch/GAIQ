package kr.co.gaiq.master.dto;

import java.math.BigDecimal;
import java.time.Instant;
import kr.co.gaiq.master.entity.Substrate;

public record SubstrateDto(
        Long substrateId,
        String substrateCode,
        String substrateType,
        String specDesignation,
        BigDecimal crossSectionMm2,
        BigDecimal diameterMm,
        Integer strandCount,
        Instant createdAt,
        Instant updatedAt) {

    public static SubstrateDto from(Substrate s) {
        return new SubstrateDto(
                s.getSubstrateId(), s.getSubstrateCode(), s.getSubstrateType(), s.getSpecDesignation(),
                s.getCrossSectionMm2(), s.getDiameterMm(), s.getStrandCount(), s.getCreatedAt(), s.getUpdatedAt());
    }
}
