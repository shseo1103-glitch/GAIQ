package kr.co.gaiq.auth.principal;

/**
 * Authenticated principal placed into Spring Security's {@code Authentication} by
 * {@code JwtAuthenticationFilter}. Carries the identifiers needed for org-scoping
 * (all business APIs except PLATFORM_ADMIN-only ones are auto-scoped by orgId at the
 * service layer).
 */
public record UserPrincipal(Long userId, Long orgId, String role) {

    public boolean isPlatformAdmin() {
        return "PLATFORM_ADMIN".equals(role);
    }
}
