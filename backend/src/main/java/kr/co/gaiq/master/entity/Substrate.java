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
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "substrate")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PUBLIC)
public class Substrate {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "substrate_id")
    private Long substrateId;

    @Column(name = "substrate_code", nullable = false, unique = true, length = 50)
    private String substrateCode;

    @Column(name = "substrate_type", nullable = false, length = 30)
    private String substrateType;

    @Column(name = "spec_designation", length = 100)
    private String specDesignation;

    @Column(name = "cross_section_mm2", precision = 10, scale = 3)
    private BigDecimal crossSectionMm2;

    @Column(name = "diameter_mm", precision = 10, scale = 4)
    private BigDecimal diameterMm;

    @Column(name = "strand_count")
    private Integer strandCount;

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
