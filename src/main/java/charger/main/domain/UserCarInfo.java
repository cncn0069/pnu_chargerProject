package charger.main.domain;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Entity
@Getter
@Setter
@ToString
@Builder
@Table( name = "user_car_info")
@AllArgsConstructor
@NoArgsConstructor
public class UserCarInfo {
	@Id@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "user_car_id")
	private Integer userCarId;
	
	@ManyToOne
	@JoinColumn(name = "car_id")
	private EVCarModel carId;
	
	@ManyToOne
	@JoinColumn(name="username")
	private Member member;
	
	private boolean enabled;
	@Column(name = "create_at")
	private LocalDateTime createAt;
}
