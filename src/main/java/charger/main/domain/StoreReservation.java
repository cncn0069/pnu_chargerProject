package charger.main.domain;

import java.time.LocalDate;
import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
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
		name = "store_reservation")
public class StoreReservation {
	@Id @GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "reserve_id")
	private Long reserve_id; 
	
	@ManyToOne
	@JoinColumn(name="user_name")
	private Member member;
	
	@ManyToOne
	@JoinColumn(name="time_id")
	private TimeSlot slot;
	
	@ManyToOne
	@JoinColumns({
		@JoinColumn(name = "stat_id",referencedColumnName = "stat_id"),
		@JoinColumn(name = "chger_id",referencedColumnName = "chger_id")})
	private Charger charger;
	
	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private ReseverState reseverState;
}
