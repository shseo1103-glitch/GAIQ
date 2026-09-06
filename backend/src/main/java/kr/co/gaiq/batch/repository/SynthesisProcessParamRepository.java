package kr.co.gaiq.batch.repository;

import java.util.List;
import kr.co.gaiq.batch.entity.SynthesisProcessParam;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SynthesisProcessParamRepository extends JpaRepository<SynthesisProcessParam, Long> {

    List<SynthesisProcessParam> findByBatchIdOrderByRecordedAtAsc(Long batchId);

    List<SynthesisProcessParam> findByBatchIdAndParamNameOrderByRecordedAtAsc(Long batchId, String paramName);
}
