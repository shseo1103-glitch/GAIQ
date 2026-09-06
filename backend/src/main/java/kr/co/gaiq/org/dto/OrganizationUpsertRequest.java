package kr.co.gaiq.org.dto;

public record OrganizationUpsertRequest(
        String orgName,
        String industryType,
        String status,
        String contactName,
        String contactEmail,
        String contactPhone) {
}
