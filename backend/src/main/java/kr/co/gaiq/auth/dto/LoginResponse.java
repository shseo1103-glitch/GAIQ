package kr.co.gaiq.auth.dto;

public record LoginResponse(String accessToken, String refreshToken, UserAccountDto user) {
}
