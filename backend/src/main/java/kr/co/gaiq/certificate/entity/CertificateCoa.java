package kr.co.gaiq.certificate.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import java.time.Instant;
import java.time.LocalDate;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 2단계 예정 테이블의 skeleton 엔티티. 1단계에서는 API/서비스 로직 없이 DB 스키마만 존재한다.
 */
@Entity
@Table(name = "certificate_coa")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PUBLIC)
public class CertificateCoa {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "certificate_id")
    private Long certificateId;

    @Column(name = "batch_id", nullable = false)
    private Long batchId;

    @Column(name = "issue_date")
    private LocalDate issueDate;

    @Column(name = "pdf_file_path", length = 500)
    private String pdfFilePath;

    /** 2단계 예정: 컬럼만 존재, NULL 허용, 실구현 없음. */
    @Column(name = "blockchain_sha256_hash", length = 100)
    private String blockchainSha256Hash;

    @Column(name = "approver_user_id")
    private Long approverUserId;

    @Column(name = "status", length = 20)
    private String status;

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
