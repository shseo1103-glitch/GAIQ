package kr.co.gaiq.audit.dto;

import java.time.Instant;
import java.util.Map;
import kr.co.gaiq.audit.entity.ActionLog;

public record ActionLogDto(
        Long actionLogId,
        Long orgId,
        Long actorUserId,
        String actorType,
        String module,
        String actionType,
        String targetEntity,
        Long targetEntityId,
        Map<String, Object> beforeData,
        Map<String, Object> afterData,
        String clientIp,
        Instant occurredAt) {

    public static ActionLogDto from(ActionLog a) {
        return new ActionLogDto(
                a.getActionLogId(), a.getOrgId(), a.getActorUserId(), a.getActorType(), a.getModule(),
                a.getActionType(), a.getTargetEntity(), a.getTargetEntityId(), a.getBeforeData(), a.getAfterData(),
                a.getClientIp(), a.getOccurredAt());
    }
}
