package kr.co.gaiq.org.dto;

public record UserAccountUpdateRequest(
        String role,
        String name,
        String email,
        String phone,
        String status) {
}
