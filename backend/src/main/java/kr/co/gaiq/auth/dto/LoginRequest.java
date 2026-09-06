package kr.co.gaiq.auth.dto;

import jakarta.validation.constraints.NotBlank;

public record LoginRequest(
        @NotBlank String orgCode,
        @NotBlank String loginId,
        @NotBlank String password) {
}
