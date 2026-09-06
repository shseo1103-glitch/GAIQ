package kr.co.gaiq.master.repository;

import kr.co.gaiq.master.entity.MeasurementEquipment;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MeasurementEquipmentRepository extends JpaRepository<MeasurementEquipment, Long> {

    boolean existsByEquipmentCode(String equipmentCode);
}
