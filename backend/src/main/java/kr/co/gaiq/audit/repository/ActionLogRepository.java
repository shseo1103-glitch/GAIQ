package kr.co.gaiq.audit.repository;

import java.time.Instant;
import kr.co.gaiq.audit.entity.ActionLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ActionLogRepository extends JpaRepository<ActionLog, Long> {

    @Query("select a from ActionLog a where "
            + "(:module is null or a.module = :module) and "
            + "(:actorUserId is null or a.actorUserId = :actorUserId) and "
            + "(:startDate is null or a.occurredAt >= :startDate) and "
            + "(:endDate is null or a.occurredAt <= :endDate)")
    Page<ActionLog> search(
            @Param("module") String module,
            @Param("actorUserId") Long actorUserId,
            @Param("startDate") Instant startDate,
            @Param("endDate") Instant endDate,
            Pageable pageable);
}
