package kr.co.gaiq.master.repository;

import kr.co.gaiq.master.entity.EquipmentModel;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EquipmentModelRepository extends JpaRepository<EquipmentModel, Long> {

    boolean existsByModelCode(String modelCode);

    Page<EquipmentModel> findByEquipmentType(String equipmentType, Pageable pageable);
}
