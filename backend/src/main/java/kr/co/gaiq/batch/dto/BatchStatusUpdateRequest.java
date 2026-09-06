package kr.co.gaiq.batch.dto;

import jakarta.validation.constraints.NotBlank;

public record BatchStatusUpdateRequest(@NotBlank String status) {
}
