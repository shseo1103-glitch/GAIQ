package kr.co.gaiq.qc.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.List;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Entity
@Table(name = "raman_spectrum_raw")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PUBLIC)
public class RamanSpectrumRaw {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "spectrum_id")
    private Long spectrumId;

    @Column(name = "batch_id", nullable = false)
    private Long batchId;

    @Column(name = "measurement_equipment_id")
    private Long measurementEquipmentId;

    @Column(name = "scan_range_cm1", length = 50)
    private String scanRangeCm1;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "wavenumber_array", nullable = false)
    private List<Double> wavenumberArray;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "intensity_array", nullable = false)
    private List<Double> intensityArray;

    @Column(name = "raw_file_path", length = 500)
    private String rawFilePath;

    @Column(name = "measured_at", nullable = false)
    private Instant measuredAt;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = Instant.now();
    }
}
