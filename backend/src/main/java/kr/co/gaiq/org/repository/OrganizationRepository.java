package kr.co.gaiq.org.repository;

import java.util.List;
import java.util.Optional;
import kr.co.gaiq.org.entity.Organization;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrganizationRepository extends JpaRepository<Organization, Long> {

    Optional<Organization> findByOrgCode(String orgCode);

    boolean existsByOrgCode(String orgCode);

    Page<Organization> findByStatus(String status, Pageable pageable);

    Page<Organization> findByOrgType(String orgType, Pageable pageable);

    Page<Organization> findByStatusAndOrgType(String status, String orgType, Pageable pageable);

    List<Organization> findByOrgNameContainingIgnoreCase(String orgName);
}
