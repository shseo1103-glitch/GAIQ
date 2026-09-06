package kr.co.gaiq.batch.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.Instant;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "synthesis_process_param")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PUBLIC)
public class SynthesisProcessParam {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "param_id")
    private Long paramId;

    @Column(name = "batch_id", nullable = false)
    private Long batchId;

    @Column(name = "param_name", nullable = false, length = 50)
    private String paramName;

    @Column(name = "param_value", nullable = false, precision = 14, scale = 4)
    private BigDecimal paramValue;

    @Column(name = "unit", nullable = false, length = 20)
    private String unit;

    @Column(name = "recorded_at", nullable = false)
    private Instant recordedAt;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @PrePersist
    protected void onCreate() {
        Instant now = Instant.now();
        this.createdAt = now;
        if (this.recordedAt == null) {
            this.recordedAt = now;
        }
    }
}
