package charger.main.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;

@Entity
public class ParkingSlot {
	@Id
	@Column(name = "slot_id")
	private Long slotId;
	
	
}
