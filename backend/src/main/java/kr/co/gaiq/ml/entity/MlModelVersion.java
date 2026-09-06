package kr.co.gaiq.ml.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.Map;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Entity
@Table(name = "ml_model_version")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PUBLIC)
public class MlModelVersion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "model_id")
    private Long modelId;

    @Column(name = "target_metric_code", nullable = false, length = 50)
    private String targetMetricCode;

    @Column(name = "algorithm", nullable = false, length = 50)
    private String algorithm;

    @Column(name = "version_no", nullable = false, length = 30)
    private String versionNo;

    @Column(name = "trained_at", nullable = false)
    private Instant trainedAt;

    @Column(name = "training_data_count", nullable = false)
    private Integer trainingDataCount;

    @Column(name = "r2_score", precision = 6, scale = 4)
    private BigDecimal r2Score;

    @Column(name = "mae", precision = 14, scale = 4)
    private BigDecimal mae;

    @Column(name = "rmse", precision = 14, scale = 4)
    private BigDecimal rmse;

    @Column(name = "kernel_type", length = 50)
    private String kernelType;

    @Column(name = "model_file_path", nullable = false, length = 500)
    private String modelFilePath;

    @Column(name = "is_active", nullable = false)
    private boolean active;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "hyperparameters_json")
    private Map<String, Object> hyperparametersJson;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = Instant.now();
    }
}
