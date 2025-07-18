package charger.main.persistence;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import charger.main.domain.ReserveTimeSlot;
import charger.main.domain.embeded.ReserveTimeId;

public interface ReserverTimeSlotRepository extends JpaRepository<ReserveTimeSlot, ReserveTimeId>{
	List<ReserveTimeSlot> findByReserveTimeId_ReserveId(Long tsId);
}
