package kr.co.gaiq.ml.dto;

import jakarta.validation.constraints.NotBlank;

public record MlPredictionCreateRequest(@NotBlank String targetMetricCode) {
}
