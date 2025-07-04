package charger.main.persistence;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import charger.main.domain.Charger;
import charger.main.domain.TimeSlot;

public interface TimeSlotRepository extends JpaRepository<TimeSlot, Long>{
	
	List<TimeSlot> findByChargerAndDate(Charger charger, LocalDate date); 
}
