package kr.co.gaiq.ml.repository;

import java.util.List;
import java.util.Optional;
import kr.co.gaiq.ml.entity.MlModelVersion;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MlModelVersionRepository extends JpaRepository<MlModelVersion, Long> {

    List<MlModelVersion> findByTargetMetricCode(String targetMetricCode);

    List<MlModelVersion> findByTargetMetricCodeAndActiveTrue(String targetMetricCode);

    Optional<MlModelVersion> findFirstByTargetMetricCodeAndActiveTrue(String targetMetricCode);
}
