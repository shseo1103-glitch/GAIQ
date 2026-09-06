package kr.co.gaiq.org.dto;

import jakarta.validation.constraints.NotBlank;

public record OnboardingRequest(
        @NotBlank String orgName,
        String industryType,
        String businessRegNo,
        @NotBlank String contactName,
        @NotBlank String contactEmail,
        String contactPhone) {
}
