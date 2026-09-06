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
@Table(name = "ml_prediction_log")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PUBLIC)
public class MlPredictionLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "prediction_id")
    private Long predictionId;

    @Column(name = "batch_id", nullable = false)
    private Long batchId;

    @Column(name = "model_id", nullable = false)
    private Long modelId;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "input_params_json", nullable = false)
    private Map<String, Object> inputParamsJson;

    @Column(name = "predicted_value", nullable = false, precision = 14, scale = 4)
    private BigDecimal predictedValue;

    @Column(name = "predicted_std_dev", precision = 14, scale = 4)
    private BigDecimal predictedStdDev;

    @Column(name = "confidence_level", length = 20)
    private String confidenceLevel;

    @Column(name = "actual_value", precision = 14, scale = 4)
    private BigDecimal actualValue;

    @Column(name = "residual", precision = 14, scale = 4)
    private BigDecimal residual;

    @Column(name = "predicted_at", nullable = false)
    private Instant predictedAt;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @PrePersist
    protected void onCreate() {
        Instant now = Instant.now();
        this.createdAt = now;
        if (this.predictedAt == null) {
            this.predictedAt = now;
        }
    }
}
