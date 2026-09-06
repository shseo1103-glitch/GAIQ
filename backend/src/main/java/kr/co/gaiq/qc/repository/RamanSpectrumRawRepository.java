package kr.co.gaiq.qc.repository;

import java.util.List;
import kr.co.gaiq.qc.entity.RamanSpectrumRaw;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RamanSpectrumRawRepository extends JpaRepository<RamanSpectrumRaw, Long> {

    List<RamanSpectrumRaw> findByBatchIdOrderByMeasuredAtDesc(Long batchId);
}
