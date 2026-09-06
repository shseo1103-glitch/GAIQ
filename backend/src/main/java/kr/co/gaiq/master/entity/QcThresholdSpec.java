package kr.co.gaiq.master.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "qc_threshold_spec")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PUBLIC)
public class QcThresholdSpec {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "threshold_id")
    private Long thresholdId;

    @Column(name = "metric_code", nullable = false, length = 50)
    private String metricCode;

    @Column(name = "metric_name", nullable = false, length = 200)
    private String metricName;

    @Column(name = "unit", nullable = false, length = 30)
    private String unit;

    @Column(name = "spec_type", nullable = false, length = 30)
    private String specType;

    @Column(name = "comparator", nullable = false, length = 10)
    private String comparator;

    @Column(name = "threshold_value", precision = 12, scale = 4)
    private BigDecimal thresholdValue;

    @Column(name = "threshold_value_max", precision = 12, scale = 4)
    private BigDecimal thresholdValueMax;

    @Column(name = "source_doc", length = 300)
    private String sourceDoc;

    @Column(name = "org_id")
    private Long orgId;

    @Column(name = "effective_from")
    private LocalDate effectiveFrom;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @PrePersist
    protected void onCreate() {
        Instant now = Instant.now();
        this.createdAt = now;
        this.updatedAt = now;
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = Instant.now();
    }
}
