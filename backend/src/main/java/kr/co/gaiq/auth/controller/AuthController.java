package kr.co.gaiq.auth.controller;

import jakarta.validation.Valid;
import kr.co.gaiq.auth.dto.LoginRequest;
import kr.co.gaiq.auth.dto.LoginResponse;
import kr.co.gaiq.auth.dto.UserAccountDto;
import kr.co.gaiq.auth.principal.CurrentUserProvider;
import kr.co.gaiq.auth.principal.UserPrincipal;
import kr.co.gaiq.auth.service.AuthService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Implements the "인증" tag of {@code 01-gaiq-core.yaml}: /auth/login, /auth/logout, /auth/me.
 */
@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final AuthService authService;
    private final CurrentUserProvider currentUserProvider;

    public AuthController(AuthService authService, CurrentUserProvider currentUserProvider) {
        this.authService = authService;
        this.currentUserProvider = currentUserProvider;
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout() {
        UserPrincipal principal = currentUserProvider.getOrNull();
        if (principal != null) {
            authService.logout(principal.userId(), principal.orgId());
        }
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/me")
    public ResponseEntity<UserAccountDto> me() {
        UserPrincipal principal = currentUserProvider.getOrThrow();
        return ResponseEntity.ok(authService.me(principal.userId()));
    }
}
