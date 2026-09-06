package kr.co.gaiq.audit.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.Map;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Entity
@Table(name = "action_log")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PUBLIC)
public class ActionLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "action_log_id")
    private Long actionLogId;

    @Column(name = "org_id")
    private Long orgId;

    @Column(name = "actor_user_id")
    private Long actorUserId;

    @Column(name = "actor_type", nullable = false, length = 20)
    private String actorType;

    @Column(name = "module", nullable = false, length = 50)
    private String module;

    @Column(name = "action_type", nullable = false, length = 20)
    private String actionType;

    @Column(name = "target_entity", nullable = false, length = 100)
    private String targetEntity;

    @Column(name = "target_entity_id")
    private Long targetEntityId;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "before_data")
    private Map<String, Object> beforeData;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "after_data")
    private Map<String, Object> afterData;

    @Column(name = "client_ip", length = 50)
    private String clientIp;

    @Column(name = "occurred_at", nullable = false)
    private Instant occurredAt;

    @PrePersist
    protected void onCreate() {
        if (this.occurredAt == null) {
            this.occurredAt = Instant.now();
        }
    }
}
