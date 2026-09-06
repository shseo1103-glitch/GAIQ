package kr.co.gaiq.audit.service;

import jakarta.servlet.http.HttpServletRequest;
import java.util.Map;
import kr.co.gaiq.audit.entity.ActionLog;
import kr.co.gaiq.audit.repository.ActionLogRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

/**
 * Writes {@code action_log} rows for CRUD/LOGIN/LOGOUT events. Called explicitly from service
 * methods (no AOP magic in 1단계) so before/after payloads can be built precisely.
 */
@Service
public class AuditRecorder {

    private final ActionLogRepository actionLogRepository;

    public AuditRecorder(ActionLogRepository actionLogRepository) {
        this.actionLogRepository = actionLogRepository;
    }

    @Transactional(propagation = Propagation.MANDATORY)
    public void record(
            Long orgId,
            Long actorUserId,
            String actorType,
            String module,
            String actionType,
            String targetEntity,
            Long targetEntityId,
            Map<String, Object> beforeData,
            Map<String, Object> afterData) {
        ActionLog log = new ActionLog();
        log.setOrgId(orgId);
        log.setActorUserId(actorUserId);
        log.setActorType(actorType);
        log.setModule(module);
        log.setActionType(actionType);
        log.setTargetEntity(targetEntity);
        log.setTargetEntityId(targetEntityId);
        log.setBeforeData(beforeData);
        log.setAfterData(afterData);
        log.setClientIp(currentClientIp());
        actionLogRepository.save(log);
    }

    public void recordUserAction(
            Long orgId,
            Long actorUserId,
            String module,
            String actionType,
            String targetEntity,
            Long targetEntityId,
            Map<String, Object> beforeData,
            Map<String, Object> afterData) {
        record(orgId, actorUserId, "USER", module, actionType, targetEntity, targetEntityId, beforeData, afterData);
    }

    private String currentClientIp() {
        HttpServletRequest request = currentRequest();
        return request != null ? request.getRemoteAddr() : null;
    }

    private HttpServletRequest currentRequest() {
        var attrs = RequestContextHolder.getRequestAttributes();
        if (attrs instanceof ServletRequestAttributes servletAttrs) {
            return servletAttrs.getRequest();
        }
        return null;
    }
}
