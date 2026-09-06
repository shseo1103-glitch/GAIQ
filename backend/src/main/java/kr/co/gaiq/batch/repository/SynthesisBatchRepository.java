package kr.co.gaiq.batch.repository;

import java.util.Optional;
import kr.co.gaiq.batch.entity.SynthesisBatch;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface SynthesisBatchRepository
        extends JpaRepository<SynthesisBatch, Long>, JpaSpecificationExecutor<SynthesisBatch> {

    Optional<SynthesisBatch> findByBatchIdAndOrgId(Long batchId, Long orgId);

    Page<SynthesisBatch> findByOrgId(Long orgId, Pageable pageable);

    boolean existsByOrgIdAndBatchNo(Long orgId, String batchNo);

    long countByOrgIdAndStatus(Long orgId, String status);

    long countByStatus(String status);

}
