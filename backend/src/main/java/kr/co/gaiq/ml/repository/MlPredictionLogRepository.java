package kr.co.gaiq.ml.repository;

import java.util.List;
import kr.co.gaiq.ml.entity.MlPredictionLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MlPredictionLogRepository extends JpaRepository<MlPredictionLog, Long> {

    List<MlPredictionLog> findByBatchIdOrderByPredictedAtDesc(Long batchId);

    Page<MlPredictionLog> findByModelIdOrderByPredictedAtDesc(Long modelId, Pageable pageable);

    long countByConfidenceLevel(String confidenceLevel);
}
