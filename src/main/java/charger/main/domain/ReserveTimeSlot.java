package charger.main.domain;

import charger.main.domain.embeded.ReserveTimeId;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MapsId;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(
		name = "reserve_time_slot")
public class ReserveTimeSlot {
	@EmbeddedId
	ReserveTimeId reserveTimeId;
	
	@MapsId("timeId")
	@ManyToOne
	@JoinColumn(name = "time_slot")
	private TimeSlot timeSlot;
	
	@MapsId("reserveId")
	@ManyToOne
	@JoinColumn(name = "reserve_id")
	private StoreReservation StoreReservation;
	
	private boolean enabled;
}
