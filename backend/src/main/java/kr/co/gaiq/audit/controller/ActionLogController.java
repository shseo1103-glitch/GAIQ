package kr.co.gaiq.audit.controller;

import java.time.LocalDate;
import kr.co.gaiq.audit.dto.ActionLogDto;
import kr.co.gaiq.audit.service.ActionLogService;
import kr.co.gaiq.auth.principal.CurrentUserProvider;
import kr.co.gaiq.common.dto.PageResponse;
import kr.co.gaiq.common.exception.PermissionDeniedException;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Implements "감사로그" tag of {@code 01-gaiq-core.yaml}. PLATFORM_ADMIN 전용.
 */
@RestController
@RequestMapping("/api/v1/audit/action-logs")
public class ActionLogController {

    private final ActionLogService actionLogService;
    private final CurrentUserProvider currentUserProvider;

    public ActionLogController(ActionLogService actionLogService, CurrentUserProvider currentUserProvider) {
        this.actionLogService = actionLogService;
        this.currentUserProvider = currentUserProvider;
    }

    @GetMapping
    public ResponseEntity<PageResponse<ActionLogDto>> list(
            @RequestParam(required = false) String module,
            @RequestParam(required = false) Long actorUserId,
            @RequestParam(required = false) LocalDate startDate,
            @RequestParam(required = false) LocalDate endDate,
            Pageable pageable) {
        requirePlatformAdmin();
        return ResponseEntity.ok(actionLogService.list(module, actorUserId, startDate, endDate, pageable));
    }

    @GetMapping("/{actionLogId}")
    public ResponseEntity<ActionLogDto> get(@PathVariable Long actionLogId) {
        requirePlatformAdmin();
        return ResponseEntity.ok(actionLogService.get(actionLogId));
    }

    private void requirePlatformAdmin() {
        var principal = currentUserProvider.getOrThrow();
        if (!principal.isPlatformAdmin()) {
            throw new PermissionDeniedException("PLATFORM_ADMIN 권한이 필요합니다.");
        }
    }
}
