package kr.co.gaiq.audit.service;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import kr.co.gaiq.audit.dto.ActionLogDto;
import kr.co.gaiq.audit.repository.ActionLogRepository;
import kr.co.gaiq.common.dto.PageResponse;
import kr.co.gaiq.common.exception.EntityNotFoundException;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Read-side of {@code action_log} — implements "감사로그" tag of {@code 01-gaiq-core.yaml}.
 * PLATFORM_ADMIN 전용(컨트롤러/시큐리티 레이어에서 role 체크는 1단계에서 서비스 호출자가 담당).
 */
@Service
public class ActionLogService {

    private final ActionLogRepository actionLogRepository;

    public ActionLogService(ActionLogRepository actionLogRepository) {
        this.actionLogRepository = actionLogRepository;
    }

    @Transactional(readOnly = true)
    public PageResponse<ActionLogDto> list(
            String module, Long actorUserId, LocalDate startDate, LocalDate endDate, Pageable pageable) {
        Instant startInstant = startDate != null ? startDate.atStartOfDay(ZoneOffset.UTC).toInstant() : null;
        Instant endInstant = endDate != null ? endDate.plusDays(1).atStartOfDay(ZoneOffset.UTC).toInstant() : null;
        return PageResponse.of(
                actionLogRepository.search(module, actorUserId, startInstant, endInstant, pageable),
                ActionLogDto::from);
    }

    @Transactional(readOnly = true)
    public ActionLogDto get(Long actionLogId) {
        return actionLogRepository.findById(actionLogId)
                .map(ActionLogDto::from)
                .orElseThrow(() -> new EntityNotFoundException("ActionLog", actionLogId));
    }
}
