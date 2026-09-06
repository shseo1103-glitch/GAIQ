package kr.co.gaiq.qc.repository;

import java.util.List;
import kr.co.gaiq.qc.entity.QcMeasurement;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface QcMeasurementRepository extends JpaRepository<QcMeasurement, Long> {

    List<QcMeasurement> findByBatchIdOrderByMeasuredAtDesc(Long batchId);

    List<QcMeasurement> findByBatchIdInOrderByMeasuredAtDesc(List<Long> batchIds, Pageable pageable);

    long countByJudgedResult(String judgedResult);
}
