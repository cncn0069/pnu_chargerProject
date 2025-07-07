package charger.main.domain;

import java.time.LocalDate;
import java.time.LocalTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinColumns;
import jakarta.persistence.ManyToOne;
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
		name = "time_slot")
public class TimeSlot {
	@Id @GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "time_id")
	private Long timeId;
	
	@ManyToOne
	@JoinColumns({
		@JoinColumn(name = "stat_id",referencedColumnName = "stat_id"),
		@JoinColumn(name = "chger_id",referencedColumnName = "chger_id")})
	private Charger charger;
	private LocalDate date;
	private LocalTime startTime;
	private LocalTime endTime;
	private boolean enabled;
}
