package kr.co.gaiq.qc.entity;

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
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "qc_measurement")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PUBLIC)
public class QcMeasurement {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "qc_measurement_id")
    private Long qcMeasurementId;

    @Column(name = "batch_id", nullable = false)
    private Long batchId;

    @Column(name = "metric_code", nullable = false, length = 50)
    private String metricCode;

    @Column(name = "measured_value", precision = 14, scale = 4)
    private BigDecimal measuredValue;

    @Column(name = "unit", nullable = false, length = 30)
    private String unit;

    @Column(name = "measurement_equipment_id")
    private Long measurementEquipmentId;

    @Column(name = "measured_at", nullable = false)
    private Instant measuredAt;

    @Column(name = "judged_result", length = 10)
    private String judgedResult;

    @Column(name = "judged_spec_type", length = 30)
    private String judgedSpecType;

    @Column(name = "measured_by_user_id")
    private Long measuredByUserId;

    @Column(name = "notes")
    private String notes;

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
