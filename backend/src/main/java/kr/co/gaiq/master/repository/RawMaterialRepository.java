package kr.co.gaiq.master.repository;

import kr.co.gaiq.master.entity.RawMaterial;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RawMaterialRepository extends JpaRepository<RawMaterial, Long> {

    boolean existsByMaterialCode(String materialCode);
}
