package kr.co.gaiq.master.repository;

import kr.co.gaiq.master.entity.Substrate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SubstrateRepository extends JpaRepository<Substrate, Long> {

    boolean existsBySubstrateCode(String substrateCode);

    Page<Substrate> findBySubstrateType(String substrateType, Pageable pageable);
}
