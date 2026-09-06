package kr.co.gaiq.batch.repository;

import java.time.Instant;
import java.util.Optional;
import kr.co.gaiq.batch.entity.SynthesisBatch;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface SynthesisBatchRepository extends JpaRepository<SynthesisBatch, Long> {

    Optional<SynthesisBatch> findByBatchIdAndOrgId(Long batchId, Long orgId);

    Page<SynthesisBatch> findByOrgId(Long orgId, Pageable pageable);

    boolean existsByOrgIdAndBatchNo(Long orgId, String batchNo);

    long countByOrgIdAndStatus(Long orgId, String status);

    long countByStatus(String status);

    @Query("select b from SynthesisBatch b where "
            + "(:orgId is null or b.orgId = :orgId) and "
            + "(:batchNo is null or b.batchNo like %:batchNo%) and "
            + "(:status is null or b.status = :status) and "
            + "(:startDate is null or b.createdAt >= :startDate) and "
            + "(:endDate is null or b.createdAt <= :endDate)")
    Page<SynthesisBatch> search(
            @Param("orgId") Long orgId,
            @Param("batchNo") String batchNo,
            @Param("status") String status,
            @Param("startDate") Instant startDate,
            @Param("endDate") Instant endDate,
            Pageable pageable);
}
