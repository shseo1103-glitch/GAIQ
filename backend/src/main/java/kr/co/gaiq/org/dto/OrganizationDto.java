package kr.co.gaiq.org.dto;

import java.time.Instant;
import kr.co.gaiq.org.entity.Organization;

public record OrganizationDto(
        Long orgId,
        String orgCode,
        String orgType,
        String orgName,
        String industryType,
        String businessRegNo,
        String status,
        String contactName,
        String contactEmail,
        String contactPhone,
        Long approvedBy,
        Instant approvedAt,
        Instant createdAt,
        Instant updatedAt) {

    public static OrganizationDto from(Organization o) {
        return new OrganizationDto(
                o.getOrgId(), o.getOrgCode(), o.getOrgType(), o.getOrgName(), o.getIndustryType(),
                o.getBusinessRegNo(), o.getStatus(), o.getContactName(), o.getContactEmail(), o.getContactPhone(),
                o.getApprovedBy(), o.getApprovedAt(), o.getCreatedAt(), o.getUpdatedAt());
    }
}
