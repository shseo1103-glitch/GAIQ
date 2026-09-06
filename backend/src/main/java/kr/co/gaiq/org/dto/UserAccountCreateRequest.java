package kr.co.gaiq.org.dto;

import jakarta.validation.constraints.NotBlank;

public record UserAccountCreateRequest(
        @NotBlank String loginId,
        @NotBlank String password,
        @NotBlank String role,
        @NotBlank String name,
        String email,
        String phone) {
}
