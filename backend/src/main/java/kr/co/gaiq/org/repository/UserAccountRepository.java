package kr.co.gaiq.org.repository;

import java.util.Optional;
import kr.co.gaiq.org.entity.UserAccount;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserAccountRepository extends JpaRepository<UserAccount, Long> {

    Optional<UserAccount> findByLoginId(String loginId);

    Optional<UserAccount> findByOrgIdAndLoginId(Long orgId, String loginId);

    boolean existsByLoginId(String loginId);

    boolean existsByEmail(String email);

    Page<UserAccount> findByOrgId(Long orgId, Pageable pageable);
}
