package kr.co.gaiq.auth.dto;

import java.time.Instant;
import kr.co.gaiq.org.entity.UserAccount;

public record UserAccountDto(
        Long userId,
        Long orgId,
        String loginId,
        String role,
        String name,
        String email,
        String phone,
        String status,
        Instant lastLoginAt,
        Instant createdAt,
        Instant updatedAt) {

    public static UserAccountDto from(UserAccount u) {
        return new UserAccountDto(
                u.getUserId(), u.getOrgId(), u.getLoginId(), u.getRole(), u.getName(),
                u.getEmail(), u.getPhone(), u.getStatus(), u.getLastLoginAt(), u.getCreatedAt(), u.getUpdatedAt());
    }
}
