package kr.co.gaiq.batch.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import java.time.Instant;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "synthesis_batch")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PUBLIC)
public class SynthesisBatch {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "batch_id")
    private Long batchId;

    @Column(name = "org_id", nullable = false)
    private Long orgId;

    @Column(name = "batch_no", nullable = false, length = 100)
    private String batchNo;

    @Column(name = "equipment_model_id", nullable = false)
    private Long equipmentModelId;

    @Column(name = "substrate_id", nullable = false)
    private Long substrateId;

    @Column(name = "raw_material_id")
    private Long rawMaterialId;

    @Column(name = "process_type", nullable = false, length = 20)
    private String processType;

    @Column(name = "started_at")
    private Instant startedAt;

    @Column(name = "ended_at")
    private Instant endedAt;

    @Column(name = "status", nullable = false, length = 20)
    private String status;

    @Column(name = "operator_user_id")
    private Long operatorUserId;

    @Column(name = "recipe_version", length = 50)
    private String recipeVersion;

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
        if (this.status == null) {
            this.status = "PLANNED";
        }
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = Instant.now();
    }
}
