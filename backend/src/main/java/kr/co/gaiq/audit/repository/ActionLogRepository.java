package kr.co.gaiq.audit.repository;

import kr.co.gaiq.audit.entity.ActionLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface ActionLogRepository
        extends JpaRepository<ActionLog, Long>, JpaSpecificationExecutor<ActionLog> {
}
