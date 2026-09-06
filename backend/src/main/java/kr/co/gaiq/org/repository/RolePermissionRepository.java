package kr.co.gaiq.org.repository;

import java.util.List;
import kr.co.gaiq.org.entity.RolePermission;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RolePermissionRepository extends JpaRepository<RolePermission, Long> {

    List<RolePermission> findByRole(String role);
}
