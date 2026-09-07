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

    /** 값을 아직 반영하지 않은(actualValue IS NULL) 해당 배쉱의 예주 로기들 — QC 실언이 뒤어오면 역산 매칭 대상.
     * modelId띌로 필털하여 서로 다륰 target_metric_code의 예주가 잘몸 매칭되는 것을 방지한다. */
    List<MlPredictionLog> findByBatchIdAndModelIdInAndActualValueIsNull(Long batchId, List<Long> modelIds);
}
