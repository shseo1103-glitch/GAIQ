package kr.co.gaiq.master.repository;

import java.util.List;
import kr.co.gaiq.master.entity.QcThresholdSpec;
import org.springframework.data.jpa.repository.JpaRepository;

public interface QcThresholdSpecRepository extends JpaRepository<QcThresholdSpec, Long> {

    List<QcThresholdSpec> findByMetricCode(String metricCode);

    List<QcThresholdSpec> findByMetricCodeAndSpecType(String metricCode, String specType);

    List<QcThresholdSpec> findByMetricCodeAndOrgIdIsNull(String metricCode);

    List<QcThresholdSpec> findByMetricCodeAndOrgId(String metricCode, Long orgId);

    List<QcThresholdSpec> findByOrgIdIsNull();

    List<QcThresholdSpec> findByOrgId(Long orgId);

    /** metric_code 기준, org 전용 스펙(orgId) 우선, 없으면 플랫폼 공통(orgId IS NULL) 순으로 조회. */
    List<QcThresholdSpec> findByMetricCodeAndOrgIdInOrderByOrgIdDesc(String metricCode, List<Long> orgIds);
}
