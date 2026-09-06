package kr.co.gaiq.org.controller;

import jakarta.validation.Valid;
import java.util.Map;
import kr.co.gaiq.auth.dto.UserAccountDto;
import kr.co.gaiq.auth.principal.CurrentUserProvider;
import kr.co.gaiq.common.dto.PageResponse;
import kr.co.gaiq.org.dto.UserAccountCreateRequest;
import kr.co.gaiq.org.dto.UserAccountUpdateRequest;
import kr.co.gaiq.org.service.UserAccountService;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Implements the "조직-사용자관리" tag's user-scoped paths of {@code 01-gaiq-core.yaml}.
 */
@RestController
@RequestMapping("/api/v1")
public class UserAccountController {

    private final UserAccountService userAccountService;
    private final CurrentUserProvider currentUserProvider;

    public UserAccountController(UserAccountService userAccountService, CurrentUserProvider currentUserProvider) {
        this.userAccountService = userAccountService;
        this.currentUserProvider = currentUserProvider;
    }

    @GetMapping("/organizations/{orgId}/users")
    public ResponseEntity<PageResponse<UserAccountDto>> listByOrg(@PathVariable Long orgId, Pageable pageable) {
        return ResponseEntity.ok(userAccountService.listByOrg(orgId, pageable));
    }

    @PostMapping("/organizations/{orgId}/users")
    public ResponseEntity<UserAccountDto> create(
            @PathVariable Long orgId, @Valid @RequestBody UserAccountCreateRequest request) {
        Long actorUserId = currentUserProvider.getOrThrow().userId();
        return ResponseEntity.status(HttpStatus.CREATED).body(userAccountService.create(orgId, request, actorUserId));
    }

    @PutMapping("/users/{userId}")
    public ResponseEntity<UserAccountDto> update(
            @PathVariable Long userId, @RequestBody UserAccountUpdateRequest request) {
        Long actorUserId = currentUserProvider.getOrThrow().userId();
        return ResponseEntity.ok(userAccountService.update(userId, request, actorUserId));
    }

    @PostMapping("/users/{userId}/reset-password")
    public ResponseEntity<Map<String, Boolean>> resetPassword(@PathVariable Long userId) {
        Long actorUserId = currentUserProvider.getOrThrow().userId();
        boolean issued = userAccountService.resetPassword(userId, actorUserId);
        return ResponseEntity.ok(Map.of("temporaryPasswordIssued", issued));
    }
}
