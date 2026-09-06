package kr.co.gaiq.auth.principal;

import kr.co.gaiq.common.exception.PermissionDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

/** Convenience accessor for the current request's authenticated {@link UserPrincipal}. */
@Component
public class CurrentUserProvider {

    public UserPrincipal getOrThrow() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof UserPrincipal principal) {
            return principal;
        }
        throw new PermissionDeniedException("인증 정보가 없습니다.");
    }

    public UserPrincipal getOrNull() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof UserPrincipal principal) {
            return principal;
        }
        return null;
    }
}
