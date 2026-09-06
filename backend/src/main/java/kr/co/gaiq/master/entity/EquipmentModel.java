package kr.co.gaiq.master.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
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
@Table(name = "equipment_model")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PUBLIC)
public class EquipmentModel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "equipment_model_id")
    private Long equipmentModelId;

    @Column(name = "model_code", nullable = false, unique = true, length = 50)
    private String modelCode;

    @Column(name = "model_name", nullable = false, length = 200)
    private String modelName;

    @Column(name = "equipment_type", nullable = false, length = 30)
    private String equipmentType;

    @Column(name = "process_type", nullable = false, length = 20)
    private String processType;

    @Column(name = "manufacturer", length = 200)
    private String manufacturer;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "spec_json")
    private Map<String, Object> specJson;

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
